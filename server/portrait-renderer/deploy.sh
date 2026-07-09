#!/usr/bin/env bash
# Build + deploy FastSD CPU (OpenVINO) portrait renderer to this Swarm node.
#   bash deploy.sh           # build image + deploy + warm up + timed test
#   bash deploy.sh rebuild   # rebuild image + redeploy only
set -euo pipefail
cd "$(dirname "$0")"

STACK=rpg-npc-fastsd
IMAGE=rpg-npc-fastsd:openvino
DOMAIN=npc-fast.colman.com.br
CADDY_IMAGE=lucaslorentz/caddy-docker-proxy:2.9.1-alpine
AUTH_FILE="$(pwd)/auth.txt"
SIBLING_AUTH="$(pwd)/../rpg-npc-image/auth.txt"
# Smoke-test with the SAME commercially-licensed config the app sends: DreamShaper-8
# (CreativeML OpenRAIL-M) + LCM-LoRA, 4 steps, tiny autoencoder. Keeps the warm-up from
# pulling the non-commercial SDXS model onto a server that serves an ad-supported app.
BASE_MODEL="Lykon/dreamshaper-8"
LCM_LORA="latent-consistency/lcm-lora-sdv1-5"

log() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }

gen_body() { # $1 = prompt
  cat <<JSON
{"prompt":"$1","negative_prompt":"","use_openvino":false,"use_lcm_lora":true,"lcm_lora":{"base_model_id":"$BASE_MODEL","lcm_lora_id":"$LCM_LORA"},"use_tiny_auto_encoder":true,"inference_steps":4,"guidance_scale":1.0,"image_width":512,"image_height":640,"diffusion_task":"text_to_image","number_of_images":1,"use_seed":false}
JSON
}

# 1) Basic-auth creds. Reuse the sd-server stack's creds so the app password works for either backend.
mkdir -p hf
if [ ! -s "$AUTH_FILE" ]; then
  if [ -s "$SIBLING_AUTH" ]; then
    log "Reusing basic-auth creds from sd-server stack"
    cp "$SIBLING_AUTH" "$AUTH_FILE"
  else
    PW="$(openssl rand -base64 24 | tr -d '/+=' | cut -c1-24)"
    log "Generating basic-auth creds (user: npc)"
    HASH="$(docker run --rm --entrypoint caddy "$CADDY_IMAGE" hash-password --plaintext "$PW" 2>/dev/null \
          || docker run --rm caddy:2 hash-password --plaintext "$PW")"
    umask 077; printf 'user: npc\npassword: %s\nbcrypt: %s\n' "$PW" "$HASH" > "$AUTH_FILE"
  fi
fi
HASH="$(grep '^bcrypt:' "$AUTH_FILE" | cut -d' ' -f2-)"
PW="$(grep '^password:' "$AUTH_FILE" | cut -d' ' -f2-)"

# 2) Render compose (double $ so stack deploy does not interpolate the bcrypt hash).
HASH_ESC="$(printf '%s' "$HASH" | sed 's/[$]/$$/g')"
awk -v h="$HASH_ESC" '{gsub(/__BCRYPT__/, h)}1' docker-compose.yml > docker-compose.rendered.yml

# 3) Build + deploy.
log "Building $IMAGE (torch CPU + openvino + deps; slow first time)"
docker build -t "$IMAGE" .
log "Building queue proxy image"
docker build -t rpg-npc-queue:latest -f Dockerfile.queue .
log "Deploying stack $STACK"
docker stack deploy --resolve-image never -c docker-compose.rendered.yml "$STACK"

log "Waiting for service to start..."
for i in $(seq 1 40); do
  st="$(docker service ps "${STACK}_fastsd" --format '{{.CurrentState}}' 2>/dev/null | head -1 || true)"
  echo "  [$i] $st"; case "$st" in Running*) break;; esac; sleep 5
done

# 4) Warm up (first call downloads + compiles the OpenVINO model — slow), then a timed run.
log "Warm-up request (downloads/compiles $OV_MODEL; may take a few minutes)"
curl -s -u "npc:$PW" -X POST "https://$DOMAIN/api/generate" -H 'Content-Type: application/json' \
     -d "$(gen_body 'a wizard portrait, fantasy')" --max-time 900 -o warmup.json || true
python3 -c "import json;d=json.load(open('warmup.json'));print('warmup latency:',d.get('latency'),'images:',len(d.get('images',[])),'err:',d.get('error'))" 2>/dev/null || echo "warmup parse failed"

log "Timed render"
time curl -s -u "npc:$PW" -X POST "https://$DOMAIN/api/generate" -H 'Content-Type: application/json' \
     -d "$(gen_body 'middle-aged hill dwarf blacksmith, fantasy character portrait, detailed face, painterly, dnd')" \
     --max-time 300 -o out.json
python3 -c "import json,base64;d=json.load(open('out.json'));img=d.get('images',[]);open('out.png','wb').write(base64.b64decode(img[0])) if img else print('NO IMAGE',str(d)[:300]);print('server latency:',d.get('latency'),'s')"
ls -la out.png 2>/dev/null

log "Done. Endpoint: https://$DOMAIN/api/generate   (creds in $AUTH_FILE)"
