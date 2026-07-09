"""Single-worker FIFO queue in front of FastSD.

FastSD renders one image at a time, so concurrent users would otherwise block invisibly. This proxy
accepts submissions immediately, assigns a queue position, and drains them serially through FastSD,
so the app can show "you're #N in line" and "generating now" and poll for the result.
"""
import asyncio
import os
import time
import uuid
from typing import Optional

import httpx
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

FASTSD_URL = os.environ.get("FASTSD_URL", "http://fastsd:8000/api/generate")
JOB_TTL_SECONDS = int(os.environ.get("JOB_TTL_SECONDS", "900"))
RENDER_TIMEOUT = float(os.environ.get("RENDER_TIMEOUT", "600"))

app = FastAPI()

jobs: dict[str, dict] = {}       # job_id -> {state, body, image, error, updated}
waiting: list[str] = []          # job ids still queued, FIFO order
current: Optional[str] = None    # job id currently rendering
_wakeup = asyncio.Event()


def _prune() -> None:
    now = time.time()
    for jid in [j for j, v in jobs.items()
                if v["state"] in ("done", "error") and now - v["updated"] > JOB_TTL_SECONDS]:
        jobs.pop(jid, None)


@app.post("/submit")
async def submit(request: Request):
    body = await request.json()
    _prune()
    jid = uuid.uuid4().hex
    jobs[jid] = {"state": "queued", "body": body, "image": None, "error": None, "updated": time.time()}
    waiting.append(jid)
    _wakeup.set()
    # ahead = jobs that must finish before this one starts.
    ahead = (len(waiting) - 1) + (1 if current is not None else 0)
    return {"job_id": jid, "ahead": ahead}


@app.get("/status/{jid}")
async def status(jid: str):
    job = jobs.get(jid)
    if job is None:
        return JSONResponse({"state": "unknown"}, status_code=404)
    if job["state"] == "queued":
        ahead = waiting.index(jid) + (1 if current is not None else 0)
    else:
        ahead = 0
    return {
        "state": job["state"],           # queued | processing | done | error
        "ahead": ahead,                  # how many render before you
        "queue_length": len(waiting) + (1 if current is not None else 0),
        "image": job["image"],           # base64 PNG when done
        "error": job["error"],
    }


@app.get("/")
async def root():
    return {"ok": True, "waiting": len(waiting), "processing": current is not None}


async def _worker():
    global current
    async with httpx.AsyncClient(timeout=httpx.Timeout(RENDER_TIMEOUT)) as client:
        while True:
            if not waiting:
                _wakeup.clear()
                await _wakeup.wait()
                continue
            jid = waiting.pop(0)
            job = jobs.get(jid)
            if job is None:
                continue
            current = jid
            job["state"] = "processing"
            job["updated"] = time.time()
            try:
                resp = await client.post(FASTSD_URL, json=job["body"])
                data = resp.json()
                images = data.get("images") or []
                if images:
                    job["image"] = images[0]
                    job["state"] = "done"
                else:
                    job["error"] = data.get("error") or f"no image (HTTP {resp.status_code})"
                    job["state"] = "error"
            except Exception as exc:  # noqa: BLE001 - report any failure back to the client
                job["error"] = str(exc)
                job["state"] = "error"
            job["updated"] = time.time()
            current = None


@app.on_event("startup")
async def _startup():
    asyncio.create_task(_worker())
