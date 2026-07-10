# RPG NPC Generator

**RPG NPC Generator** is an Android app that helps Dungeon Masters create
non-player characters for their tabletop campaigns — rolling up a believable NPC
in seconds, saving the ones worth keeping, and even generating a portrait to go
with them.

It aims to be easy yet powerful, so DMs can spend their creativity where it
matters and enrich their players' experience.

<a href='https://play.google.com/store/apps/details?id=me.kerooker.rpgcharactergenerator&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png'/></a>

## Features

- **Randomize an NPC** — roll a complete D&D-flavoured character: race, age,
  gender, profession, alignment, personality, motivation, languages, name and
  nickname. Re-roll any single trait until it fits your scene.
- **AI portraits** — generate a fantasy portrait from the NPC's own traits,
  rendered on a self-hosted server and delivered in the background
  (see [`server/portrait-renderer`](server/portrait-renderer/README.md)).
- **Save & browse** — keep the NPCs you like and revisit them any time; each
  retains its attributes and portrait.
- **Offline-first** — attribute generation and your saved roster work with no
  network; only portrait rendering reaches out.
- **Localised** — available in English and Portuguese.

## Tech stack

100% Kotlin. Jetpack Compose (Material 3) UI, SQLDelight persistence, Koin for
dependency injection, WorkManager for background portrait jobs and coil3 for
image loading. Business logic is tested with Kotest and UI components with
Robolectric; Detekt and Android Lint run in CI.

## Project structure

- **`:app`** — the Android application (`me.kerooker.rpgnpcgenerator`).
- **`:portrait-queue`** ([`server/portrait-renderer`](server/portrait-renderer/README.md))
  — a small Kotlin/Ktor FIFO queue fronting the CPU image renderer the app calls
  for portraits. Runs as a Docker Swarm stack and is not needed to build or run
  the app.

## Building & running

The app targets Android (minSdk 26, targetSdk 36) and builds with JDK 17+
(CI uses JDK 21).

```bash
./gradlew :app:assembleDebug    # build the debug APK
./gradlew :app:installDebug     # install on a connected device/emulator
./gradlew test                  # run the Kotest + Robolectric tests
```

Portrait generation talks to a private render server; pass its basic-auth
password with `-PnpcImagePassword=<password>` to exercise that feature in a local
build. Without it the app still runs and portrait requests simply fail cleanly.

## Contributing

Feel free to create an issue or open a PR for whatever you feel like. We're open to all sorts of discussions and improvements!

## Releasing & signing

Releases are automated — see [RELEASE.md](RELEASE.md). The signing keystore and the Google Play
service-account key are stored encrypted with [git-secret](https://sobolevn.me/git-secret/) and are
revealed in CI using a dedicated, project-only GPG key (no personal keys are used). That key's
**public** half is published below so anyone can inspect the recipient the secrets are encrypted to.

Release GPG public key — fingerprint `344F 705A AD16 71B7 094A E0BA AD7A B4BA 96BB BEB3`:

```
-----BEGIN PGP PUBLIC KEY BLOCK-----

mQINBGpP24EBEADweZMFBOY5idF4btud8WCqexq24iKl2iC/EP5QbYLMQvPjp/Oh
5gnaEkn64vUk28pBFbN8yCKfC+dQ1zsiEBoRWVnTKdkPlnz1IQ8znO34EkCHnlvt
r3hStee1VnnpxFsPz/ShGjB6Bw8PgraTazmLO5wEDOhqTbrLigBQOv2YfbBusqHE
TWhHgr7s05SpKFKri2VyRxHfoO0/POLaU3HoWqzjX1pzDDnUoIzAmZ/ZkUhjBtoS
C6ZKUJ42csoJbdfu7sQ2+zz40aljqIVVFiSa94lf5xC5bR/hzKrmNCthomaZF5DH
Ch4p1rY452awroOHtleKdibdQyZ5zRDSvFX3OYZmsEw/I4naHILTi/FHOKKjSBY7
O7/nnJMNeerm4PE3HiEYPp4SfgnAqKcVPzZjwBwMahprut36yPFb0H2GmBqix+jL
RPxO5lb3vIUObxyuFkvTjDvGDg6tEbqAtEPDZdke3N8oFUfNIXDgPGI1woTMay+f
vpV4sbQUrIUaqEwNgqB1RMmXoj02QkC4jZm4fURxvORRY6uwd05hDsJ36pfZjs7e
drvXrq6fwVUn+Qi8iDe/GskDMbQxE71ezemi0aJSLmMlRNaKBqil2/005Lef53AM
nNSfFrk2XJkdNuB264wMnGK36y78e7E2vUiPzQboyC02Vj1QSzkEXdWe3QARAQAB
tDdSUEcgTlBDIEdlbmVyYXRvciBSZWxlYXNlIDxyZWxlYXNlQHJwZ25wY2dlbmVy
YXRvci5hcHA+iQJuBBMBCABYFiEENE9wWq0WcbcJSuC6rXq0upa7vrMFAmpP24Eb
FIAAAAAABAAObWFudTIsMi41KzEuMTIsMCwyAxsvBAULCQgHAgIiAgYVCgkICwIE
FgIDAQIeBwIXgAAKCRCterS6lru+szetEADsnO6QJeLCinHrKr3FhpIICP0DaJst
LPvzI34jzE6lzBqDhQIf23Fa5X/t87Yepy+TZVXLMtDzg2k9DvrL7EnJxNoldBJX
e/ERATpTtiVpmCGic90bW5MVdx2j0At7/FlGqF7vpTFkslpiPYFNhRlVH8YMGcMe
bh9q/yx9wSRKM8Gf1knpb9+D/oNPqod7SYTOMUtWqX4Ym6BvPsYNXNR8BH06jjj6
qir5A0INADuHUjhPtN8UHwGR/ZI0l4fTvWCiemx5vwq3BoQJZDB03dQVAr6GgnvX
wru2Bd9hse6UhxKSTXW7RKksa226Qvo5ZuCUAy+ekCC96HhIhX2X2YTql56Ry3fu
9nX7TOB8weKttDwVViZ+DtWCh/qD/1ITwaTKz714tqFoiaOhpgYbkyu8oTdsWBvc
cQ1NgWZ52ihU/iSWurnm/vhRQYBrRT9QVWvn42gMgdtFo09lkzBS7297kM8siXs9
P/FdfZAuY0nKHrBJ47puUeBCPCnPA/TXICZvCTO49SnCg3TZpl4FMHDiRFHvSm6i
1GKvOgAdhUS3u5d97dobdOZOLTRNCbXNeQRcRZMbij31xcCnGIoM+MJm7BDe/iXt
WrkpVAwpPVomcuqKXvOpuvsipbcytkoOZGu5+gQ/E7u8aWSTt9GAe6XchuN65ZHN
ojD+C+gO6t2gyw==
=VUVq
-----END PGP PUBLIC KEY BLOCK-----
```

## License

RPG NPC Generator is licensed under the **GNU Affero General Public License v3.0**
(AGPL-3.0). See [LICENSE](LICENSE).
