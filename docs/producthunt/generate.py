#!/usr/bin/env python3
"""Regenerate the Product Hunt gallery kit from the app screenshots.

Renders landscape 1270x760 gallery slides (a hero + one per feature) and a
240x240 thumbnail. Each slide is an HTML/CSS card with the phone screenshot on
a themed background; it is rendered with headless Chrome at 2x and downscaled
for crisp text.

Inputs : docs/screenshots/*.png (the phone shots) and the app launcher icon.
Outputs: the PNGs next to this script (docs/producthunt/).

Requires: google-chrome (or chromium) and ImageMagick's `convert` on PATH.

Usage: python3 docs/producthunt/generate.py
"""
import base64
import pathlib
import shutil
import subprocess
import sys

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SHOTS = ROOT / "docs" / "screenshots"
ICON = ROOT / "app" / "src" / "main" / "res" / "mipmap-xxxhdpi" / "ic_launcher.png"
BUILD = HERE / "_build"


def b64(path: pathlib.Path) -> str:
    return "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode()


def chrome_binary() -> str:
    for name in ("google-chrome", "google-chrome-stable", "chromium", "chromium-browser"):
        if shutil.which(name):
            return name
    sys.exit("No Chrome/Chromium found on PATH.")


CSS = """
*{margin:0;padding:0;box-sizing:border-box}
html,body{width:1270px;height:760px;overflow:hidden}
body{font-family:"Liberation Sans","DejaVu Sans",Arial,sans-serif;
  -webkit-font-smoothing:antialiased;background:#0a0b14;color:#f2f4fb}
.slide{position:relative;width:1270px;height:760px;overflow:hidden;
  background:
    radial-gradient(900px 600px at 88% 8%, rgba(120,132,255,.28), transparent 60%),
    radial-gradient(760px 620px at 6% 108%, rgba(196,90,220,.18), transparent 60%),
    linear-gradient(140deg,#15162c 0%,#0c0d1a 55%,#08090f 100%)}
.slide:after{content:"";position:absolute;inset:0;
  box-shadow:inset 0 0 240px rgba(0,0,0,.55);pointer-events:none}
.brand{display:flex;align-items:center;gap:16px}
.brand img{width:52px;height:52px;border-radius:13px;box-shadow:0 6px 18px rgba(0,0,0,.5)}
.brand span{font-size:25px;font-weight:700;letter-spacing:.2px;color:#eef0fb}
.eyebrow{font-size:19px;font-weight:700;letter-spacing:3px;text-transform:uppercase;color:#9aa6ff}
.chips{display:flex;gap:12px;flex-wrap:wrap}
.chip{font-size:18px;color:#c4cae0;border:1px solid rgba(154,166,255,.32);
  border-radius:999px;padding:9px 18px;background:rgba(120,132,255,.06)}
.device{position:relative;background:#04050a;padding:9px;border-radius:40px;
  box-shadow:0 45px 90px rgba(0,0,0,.62),0 0 0 1px rgba(255,255,255,.06),0 0 0 8px rgba(120,132,255,.05)}
.device img{display:block;border-radius:32px}
"""

FEATURE = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%}}
.left{{flex:1;padding:78px 40px 78px 88px;display:flex;flex-direction:column}}
.brand{{margin-bottom:auto}}
.eyebrow{{margin-bottom:22px}}
.headline{{font-size:62px;font-weight:800;line-height:1.05;letter-spacing:-1px;max-width:620px}}
.body{{font-size:26px;line-height:1.45;color:#aab2ca;margin-top:26px;max-width:600px}}
.chips{{margin-top:40px}}
.right{{width:560px;display:flex;align-items:center;justify-content:center;padding-right:20px;transform:rotate(2.2deg)}}
.device img{{height:636px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="left">
  <div class="brand"><img src="{icon}"><span>RPG NPC Generator</span></div>
  <div class="eyebrow">{eyebrow}</div>
  <div class="headline">{headline}</div>
  <div class="body">{body}</div>
  <div class="chips">{chips}</div>
</div>
<div class="right"><div class="device"><img src="{shot}"></div></div>
</div></div></body></html>"""

HERO = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;align-items:center}}
.left{{flex:1;padding:0 30px 0 96px}}
.brand{{margin-bottom:40px}}
.brand img{{width:76px;height:76px;border-radius:19px}}
.brand span{{font-size:40px;font-weight:800;letter-spacing:-.5px}}
.tagline{{font-size:52px;font-weight:800;line-height:1.08;letter-spacing:-1px;max-width:640px;color:#f6f7fe}}
.tagline b{{color:#aeb8ff}}
.sub{{font-size:25px;color:#aab2ca;margin-top:26px;max-width:560px;line-height:1.4}}
.chips{{margin-top:40px}}
.right{{width:600px;display:flex;align-items:center;justify-content:center;position:relative}}
.device.back{{position:absolute;transform:rotate(-9deg) translateX(-120px) scale(.9);opacity:.62;filter:saturate(.9)}}
.device.front{{transform:rotate(4deg) translateX(60px)}}
.device img{{height:604px}}
.device.front img{{height:648px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="left">
  <div class="brand"><img src="{icon}"><span>RPG NPC Generator</span></div>
  <div class="tagline">Instant D&amp;D NPCs with <b>AI portraits</b>, ready for your table</div>
  <div class="sub">Roll a complete, believable NPC in seconds &mdash; name, race, personality, a portrait and more.</div>
  <div class="chips">
    <div class="chip">Free on Google Play</div>
    <div class="chip">Offline-first</div>
    <div class="chip">100% open source</div>
  </div>
</div>
<div class="right">
  <div class="device back"><img src="{back}"></div>
  <div class="device front"><img src="{front}"></div>
</div>
</div></div></body></html>"""


def chips(*items):
    return "".join(f'<div class="chip">{i}</div>' for i in items)


def main():
    icon = b64(ICON)
    img = {n: b64(SHOTS / f"{n}.png") for n in ["randomize", "roster", "detail", "combat", "backup"]}

    slides = {
        "1_hero": HERO.format(css=CSS, icon=icon, front=img["randomize"], back=img["roster"]),
        "2_randomize": FEATURE.format(
            css=CSS, icon=icon, shot=img["randomize"], eyebrow="Roll an NPC",
            headline="A complete NPC in one tap",
            body="Race, profession, alignment, personality, motivation, languages and a name. "
                 "Re-roll any single trait until it fits your scene.",
            chips=chips("Instant", "Re-roll anything")),
        "3_detail": FEATURE.format(
            css=CSS, icon=icon, shot=img["detail"], eyebrow="AI portraits",
            headline="A face for every character",
            body="Each NPC gets a fantasy portrait generated from its own traits, delivered in "
                 "the background while you keep playing.",
            chips=chips("Trait-driven", "Background render")),
        "4_combat": FEATURE.format(
            css=CSS, icon=icon, shot=img["combat"], eyebrow="Combat ready",
            headline="Optional D&amp;D 5e stat block",
            body="Add ability scores with modifiers, armor class, hit points and challenge rating "
                 "whenever an NPC needs to fight.",
            chips=chips("Abilities &amp; modifiers", "AC &middot; HP &middot; CR")),
        "5_backup": FEATURE.format(
            css=CSS, icon=icon, shot=img["backup"], eyebrow="Own your roster",
            headline="Back up &amp; restore anywhere",
            body="Save, search and group your NPCs by campaign &mdash; then export the whole roster "
                 "to one file, portraits included, and restore it on any device.",
            chips=chips("Search &amp; campaigns", "Portraits included")),
    }

    BUILD.mkdir(exist_ok=True)
    chrome = chrome_binary()
    for name, html in slides.items():
        page = BUILD / f"{name}.html"
        page.write_text(html, encoding="utf-8")
        subprocess.run([
            chrome, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
            "--force-device-scale-factor=2", "--window-size=1270,760", "--virtual-time-budget=2000",
            f"--screenshot={BUILD / (name + '_2x.png')}", page.as_uri(),
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        subprocess.run(["convert", str(BUILD / f"{name}_2x.png"), "-resize", "1270x760",
                        str(HERE / f"{name}.png")], check=True)
        print(f"rendered {name}.png")

    subprocess.run(["convert", str(ICON), "-resize", "240x240", "-strip",
                    str(HERE / "thumbnail.png")], check=True)
    print("rendered thumbnail.png")
    shutil.rmtree(BUILD, ignore_errors=True)


if __name__ == "__main__":
    main()
