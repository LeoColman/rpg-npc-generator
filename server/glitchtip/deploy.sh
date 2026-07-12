#!/usr/bin/env bash
# Deploy GlitchTip (self-hosted crash reporting) to this Swarm node.
#   bash deploy.sh             # generate secrets if needed, migrate, deploy, smoke-test
#   bash deploy.sh bootstrap   # + create the owner account, org and project, print the DSN
#
# Run ON the swarm node (ritalee); the caddy overlay network must already exist.
set -euo pipefail
cd "$(dirname "$0")"

STACK=glitchtip
DOMAIN=glitchtip.colman.com.br
DATA_DIR=/root/manual-stacks/glitchtip/pgdata
ENV_FILE="$(pwd)/.env"
MODE="${1:-deploy}"

log() { printf '\n\033[1;36m==> %s\033[0m\n' "$*"; }

# 1) Secrets. Generated once and kept out of git (.gitignore); regenerating POSTGRES_PASSWORD
#    after the first deploy would NOT change the role in an existing database — see .env.example.
if [ ! -s "$ENV_FILE" ]; then
  log "Generating .env (SECRET_KEY + POSTGRES_PASSWORD)"
  umask 077
  {
    printf 'SECRET_KEY=%s\n' "$(openssl rand -base64 48 | tr -d '/+=\n' | cut -c1-50)"
    printf 'POSTGRES_PASSWORD=%s\n' "$(openssl rand -base64 32 | tr -d '/+=\n' | cut -c1-32)"
  } > "$ENV_FILE"
fi
set -a; . "$ENV_FILE"; set +a
: "${SECRET_KEY:?missing in .env}" "${POSTGRES_PASSWORD:?missing in .env}"
IMAGE="glitchtip/glitchtip:${GLITCHTIP_TAG:-6.2.0}"

mkdir -p "$DATA_DIR"

# 2) Deploy. `docker stack deploy` interpolates ${SECRET_KEY}/${POSTGRES_PASSWORD} from the
#    environment we just sourced, so no rendered copy of the compose file ever hits disk.
log "Deploying stack $STACK ($IMAGE)"
docker stack deploy -c docker-compose.yml "$STACK"

log "Waiting for postgres..."
for i in $(seq 1 30); do
  st="$(docker service ps "${STACK}_postgres" --format '{{.CurrentState}}' 2>/dev/null | head -1 || true)"
  echo "  [$i] $st"; case "$st" in Running*) sleep 5; break;; esac; sleep 5
done

# 3) Migrations. The all-in-one image already migrates on boot; this is an idempotent belt-and-braces
#    run (Swarm has no run-once job primitive, so it goes through a one-off container on the overlay).
log "Running database migrations"
docker run --rm --network "${STACK}_default" \
  -e DATABASE_URL="postgres://glitchtip:${POSTGRES_PASSWORD}@postgres:5432/glitchtip" \
  -e VALKEY_URL="redis://valkey:6379" \
  -e SECRET_KEY="$SECRET_KEY" \
  "$IMAGE" ./manage.py migrate --no-input

log "Waiting for the web service..."
for i in $(seq 1 40); do
  code="$(curl -s -o /dev/null -w '%{http_code}' "https://$DOMAIN/" --max-time 10 || true)"
  echo "  [$i] https://$DOMAIN -> $code"
  case "$code" in 200|302) break;; esac
  sleep 5
done

if [ "$MODE" = "bootstrap" ]; then
  log "Bootstrapping owner account, organization and project"
  # Registration is disabled on this instance, so the first (and only) account is created here.
  # Prints the DSN the Android app needs; the password is written to owner-credentials.txt.
  OWNER_EMAIL="${OWNER_EMAIL:-leonardo@colman.com.br}"
  OWNER_PASSWORD="${OWNER_PASSWORD:-$(openssl rand -base64 18 | tr -d '/+=\n' | cut -c1-18)}"
  docker run --rm --network "${STACK}_default" \
    -e DATABASE_URL="postgres://glitchtip:${POSTGRES_PASSWORD}@postgres:5432/glitchtip" \
    -e VALKEY_URL="redis://valkey:6379" \
    -e SECRET_KEY="$SECRET_KEY" \
    -e OWNER_EMAIL="$OWNER_EMAIL" \
    -e OWNER_PASSWORD="$OWNER_PASSWORD" \
    -e GLITCHTIP_DOMAIN="https://$DOMAIN" \
    "$IMAGE" ./manage.py shell -c '
import os
from django.contrib.auth import get_user_model
# GlitchTip 6.x namespaces its Django apps under apps.* (they were top-level in 5.x).
from apps.organizations_ext.models import Organization
from apps.projects.models import Project, ProjectKey

User = get_user_model()
email, password = os.environ["OWNER_EMAIL"], os.environ["OWNER_PASSWORD"]
user, created = User.objects.get_or_create(email=email)
if created:
    user.is_staff = user.is_superuser = True
    user.set_password(password)
    user.save()
    print("USER_CREATED", email)
else:
    print("USER_EXISTS", email)

org, _ = Organization.objects.get_or_create(name="Colman")
if not org.users.filter(pk=user.pk).exists():
    org.add_user(user)

project, _ = Project.objects.get_or_create(
    organization=org, name="rpg-npc-generator", defaults={"slug": "rpg-npc-generator"}
)
# A ProjectKey is normally created by a signal on project creation; make sure of it either way.
key = ProjectKey.objects.filter(project=project).first() or ProjectKey.objects.create(project=project)
host = os.environ["GLITCHTIP_DOMAIN"].split("//")[1]
print("DSN https://%s@%s/%s" % (key.public_key, host, project.id))
' | tee bootstrap-output.txt

  if grep -q USER_CREATED bootstrap-output.txt; then
    umask 077
    printf 'email: %s\npassword: %s\n' "$OWNER_EMAIL" "$OWNER_PASSWORD" > owner-credentials.txt
    log "Owner credentials written to owner-credentials.txt (gitignored)"
  fi
fi

log "Done. UI: https://$DOMAIN"
grep '^DSN' bootstrap-output.txt 2>/dev/null || true
