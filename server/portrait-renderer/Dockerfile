# FastSD CPU (OpenVINO) REST API for RPG portrait rendering. OpenVINO + SDXS-512-0.9 is the
# Intel-optimized path (~0.8s/image on an i7-12700). Pinned to a known-good commit.
FROM ghcr.io/astral-sh/uv:python3.11-trixie-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
        git curl build-essential libgl1 libglib2.0-0 ca-certificates && \
    rm -rf /var/lib/apt/lists/*

ENV DEVICE=cpu
WORKDIR /app

ARG FASTSD_COMMIT=0429540866dad888b491f0b33d47b2b7fe76a684
RUN git clone https://github.com/rupeshs/fastsdcpu . && git checkout ${FASTSD_COMMIT}

# Honor use_safety_checker. Upstream unconditionally strips the diffusers safety checker before every
# render, so NSFW never gets filtered. Gate that on the request flag so use_safety_checker=true KEEPS
# the checker (DreamShaper-8 ships one), blanking nude/explicit output. Fails the build if unmatched.
RUN sed -i 's|^            self\.pipeline\.safety_checker = None$|            if not lcm_diffusion_setting.use_safety_checker:\n                self.pipeline.safety_checker = None|' src/backend/lcm_text_to_image.py \
 && grep -q "if not lcm_diffusion_setting.use_safety_checker" src/backend/lcm_text_to_image.py

# CPU-only torch, then the rest of FastSD's deps.
RUN uv pip install --system --no-cache-dir torch==2.8.0 torchvision==0.23.0 \
        --index-url https://download.pytorch.org/whl/cpu
RUN uv pip install --system --no-cache-dir -r requirements.txt

EXPOSE 8000
ENV PYTHONUNBUFFERED=1
CMD ["python", "src/app.py", "--api"]
