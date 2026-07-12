# GlitchTip — crash reporting

Self-hosted crash reporting for the Android app. GlitchTip implements the Sentry protocol, so the
app uses the stock `io.sentry:sentry-android` SDK and reports here instead of to a third party — no
Google/Crashlytics dependency in a FOSS, ad-supported app.

- **UI:** <https://glitchtip.colman.com.br> (owner login only — registration is disabled)
- **Runs on:** ritalee, Docker Swarm, exposed through caddy-docker-proxy like the other stacks
- **App side:** `app/src/main/java/.../crash/CrashReporting.kt`, DSN baked in as `BuildConfig.GLITCHTIP_DSN`

## Deploy

Run **on ritalee** (the `caddy` overlay network must exist):

```bash
cd /root/manual-stacks/glitchtip
bash deploy.sh             # secrets (first run) + stack + migrations + smoke test
bash deploy.sh bootstrap   # the above, plus owner account + org + project; prints the DSN
```

`deploy.sh` generates `.env` (`SECRET_KEY`, `POSTGRES_PASSWORD`) on first run and `docker stack
deploy` interpolates it, so no rendered file with secrets ever lands on disk. Both `.env` and
`owner-credentials.txt` are gitignored; keep a copy of the latter somewhere safe.

To sync changes from the repo:

```bash
tar czf - -C server glitchtip | ssh root@ritalee.colman.com.br 'tar xzf - -C /root/manual-stacks/'
```

## Services

| Service | What it does |
|---|---|
| `glitchtip` | web UI + event ingest + embedded worker (`run-all-in-one.sh`), port 8000 behind caddy |
| `postgres` | issue/event storage, bind-mounted at `/root/manual-stacks/glitchtip/pgdata` |
| `valkey` | cache + task queue |

One all-in-one container instead of separate web/worker: right-sized for a single low-volume app.
If the event rate ever outgrows it, split the command into `run-granian.sh` + `run-worker.sh`.

## Notes and gotchas

- **The instance is public** (the app must POST crashes to it from anywhere), so `ENABLE_USER_REGISTRATION`
  and `ENABLE_OPEN_USER_REGISTRATION` are **false** — otherwise strangers could sign up. The ingest
  endpoint needs no auth by design: a DSN is a write-only key.
- **The overlay network is `attachable: true`** because `deploy.sh` runs migrations and bootstrap in
  one-off `docker run` containers; Swarm rejects plain containers on a non-attachable overlay.
- **GlitchTip 6.x namespaces its Django apps under `apps.*`** (`apps.projects.models`, not
  `projects.models`) — the bootstrap shell snippet depends on this.
- The image migrates on boot; `deploy.sh` runs `migrate` anyway (idempotent) so a deploy fails loudly
  rather than silently serving an unmigrated schema.
- Events older than `GLITCHTIP_MAX_EVENT_LIFE_DAYS` (90) are pruned, keeping postgres small.
- No SMTP is configured (`EMAIL_URL=consolemail://`): new-issue mails go to the container log. Set a
  real `EMAIL_URL` to get notified.

## Verifying it works

Debug builds have no DSN, so crash reporting is off by default. To test the real path:

```bash
./gradlew :app:installDebug -PglitchtipDsn='https://<key>@glitchtip.colman.com.br/1'
```

then Settings → **Force a test crash (debug only)**, reopen the app (the SDK flushes the cached
envelope on next start) and the crash shows up in the GlitchTip UI.
