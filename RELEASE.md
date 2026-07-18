# Releasing

Releases are automated with a GitHub Action (`.github/workflows/release.yaml`) and Fastlane,
following the same pattern as the Petals project.

## What the release does

Trigger **Actions → Release → Run workflow**, choose a bump type (`major` / `minor` / `patch`)
and type the changelog. The workflow then:

1. Reveals the signing/Play secrets with git-secret.
2. Runs `app/bump_version.sh`, which bumps the literal `versionCode`/`versionName` in `app/build.gradle.kts`
   (kept literal so F-Droid can parse them for tag-based auto-updates), writes the Play changelog to
   `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`, then commits, tags and pushes.
3. Builds the signed release APK and creates a GitHub Release (APK + R8 mapping).
4. Builds the signed release AAB and uploads it (plus the store listing) to Google Play with
   `fastlane playstore`.

`versionCode = major*10000 + minor*100 + patch` (matches `app/build.gradle.kts`).

## Secrets

The Play service-account key (`fastlane/google-play.json`), the upload keystore (`app/keystore`)
and its passwords (`app/keystore.properties`) are **never committed in plaintext**. They are stored
encrypted as `*.secret` via git-secret and revealed in CI.

### Already configured

- git-secret is initialized with a **dedicated, passphrase-less project key**
  `RPG NPC Generator Release <release@rpgnpcgenerator.app>`
  (fingerprint `344F 705A AD16 71B7 094A E0BA AD7A B4BA 96BB BEB3`) — no personal keys are used.
  Its public half is published in [README.md](README.md).
- The three secret files are committed as `app/keystore.secret`,
  `app/keystore.properties.secret` and `fastlane/google-play.json.secret`.
- The keystore properties keys are `STORE_FILE`, `KEYSTORE_PASSWORD`, `SIGNING_KEY_ALIAS`,
  `SIGNING_KEY_PASSWORD`.

### Still to do (GitHub → Settings → Secrets and variables → Actions)

1. **`GPG_KEY`** — the ASCII-armored **private** key that decrypts the git-secret files. It was
   exported to `~/Downloads/rpg-npc-release-gpg-private.asc`; paste its full contents into the
   secret, then delete that file.
2. **`RELEASE_KEY`** — an SSH deploy key with **write** access, so the version-bump commit and tag
   can be pushed back (`ssh-keygen -t ed25519`; add the public half as a repo deploy key with write
   access, put the private half in this secret).
3. In the **Play Console**, upload the first AAB for the track manually (Play requires it once);
   afterwards the workflow publishes every release.

Local/PR builds without the secrets still compile — the release variant is simply left unsigned.

> To decrypt locally, import the dedicated key and run `git secret reveal`. To change who can
> decrypt: `git secret tell <email>` / `git secret killperson <email>`, then `git secret hide`.
