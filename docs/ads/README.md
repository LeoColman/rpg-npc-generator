# Google Ads kit

Complete asset kit for a **Google Ads App campaign** (app installs) for
[RPG NPC Generator on Google Play](https://play.google.com/store/apps/details?id=me.kerooker.rpgcharactergenerator),
plus the script that produces the image and video assets. Marketing only —
nothing here ships in the app.

App campaigns don't use keywords or manual placements: you provide text,
image and video assets and Google mixes them across Search, Play, YouTube
and Display. Asset limits: **5 headlines (≤30 chars), 5 descriptions
(≤90 chars), up to 20 images, 20 YouTube videos and 20 HTML5 ZIPs.**

## Campaign setup (recommended)

| Setting | Value |
|---|---|
| Campaign type | App — App installs |
| App | `me.kerooker.rpgcharactergenerator` (Google Play) |
| Campaign 1 | pt-BR assets · Brazil · Portuguese |
| Campaign 2 | en-US assets · Worldwide (or US/CA/UK/AU/DE/PL) · English |
| Bidding | Install volume, no target CPA for the first 2–3 weeks, then set tCPA from observed CPI |
| Budget | Google recommends ≥50× your expected CPI per day; start small and scale what converts |

Keep the two languages in **separate campaigns** — App campaigns don't
translate assets, and mixing languages pollutes asset ratings.

## Text assets — pt-BR

Headlines (≤30 chars — top 5 first, rest are swap-ins for asset rotation):

1. `NPCs de D&D em um toque` (23)
2. `Retratos de IA para NPCs` (24)
3. `O mestre nunca mais trava` (25)
4. `NPC completo em segundos` (24)
5. `Grátis e código aberto` (22)
6. `Gere NPCs na hora` (17)
7. `Ficha 5e opcional inclusa` (25)
8. `Improvise qualquer NPC` (22)
9. `Sua taverna sempre cheia` (24)
10. `Crie NPCs para seu RPG` (22)

Descriptions (≤90 chars):

1. `Nome, raça, profissão, personalidade e retrato de IA em um toque. Salve e edite tudo.` (85)
2. `Precisa de um taverneiro agora? Gere NPCs completos na hora e salve seus favoritos.` (83)
3. `Gere, edite e salve NPCs com retrato de IA. Ficha de D&D 5e opcional para combate.` (82)
4. `App grátis e open source para mestres de RPG. NPCs críveis em segundos, até offline.` (84)
5. `Re-role qualquer campo até encaixar na cena. Backup completo do seu elenco de NPCs.` (83)

## Text assets — en-US

Headlines (≤30 chars — top 5 first):

1. `Instant D&D NPCs` (16)
2. `AI portraits for every NPC` (26)
3. `A full NPC in one tap` (21)
4. `Never stall your session` (24)
5. `Free & open source` (18)
6. `Optional 5e stat blocks` (23)
7. `NPCs ready for your table` (25)
8. `Roll name, race & story` (23)

Descriptions (≤90 chars):

1. `Name, race, profession, personality and an AI portrait in one tap. Save and edit all.` (85)
2. `Need an innkeeper right now? Roll complete, believable NPCs and save your favorites.` (84)
3. `Generate, edit and save NPCs with AI portraits. Optional D&D 5e stat block for combat.` (86)
4. `Free, open-source app for game masters. Believable NPCs in seconds, even offline.` (81)
5. `Re-roll any single trait until it fits the scene. Back up your whole roster anywhere.` (85)

## Long copy (YouTube video descriptions / landing page)

**pt-BR** — Sessão hoje e nenhum NPC preparado? O Gerador de NPC para RPG
cria personagens completos em um toque: nome, raça, profissão, tendência,
motivação, personalidade e um retrato de fantasia gerado por IA. Re-role
qualquer campo até encaixar na cena, adicione uma ficha de D&D 5e quando o
NPC precisar lutar e faça backup de todo o seu elenco. Grátis, open source
e funciona offline. Baixe no Google Play.

**en-US** — Session tonight and no NPCs prepared? RPG NPC Generator rolls
complete characters in one tap: name, race, profession, alignment,
motivation, personality and an AI-generated fantasy portrait. Re-roll any
single trait until it fits the scene, add a D&D 5e stat block when a fight
breaks out, and back up your whole roster. Free, open source and works
offline. Get it on Google Play.

## Image assets (generated)

5 themes × the 3 Google-recommended sizes = **15 images per language**
(the campaign limit is 20). Files follow `image_{lang}_{theme}_{size}.png`:

| Size label | Pixels | Ratio |
|---|---|---|
| `landscape` | 1200×628 | 1.91:1 |
| `square` | 1200×1200 | 1:1 |
| `portrait` | 1200×1500 | 4:5 |

| Theme | Message | Screenshot |
|---|---|---|
| `hero` | Tagline + feature summary | `randomize` |
| `randomize` | "Um toque. NPC completo." / "One tap. A full NPC." | `randomize` |
| `detail` | AI portrait for every NPC | `detail` |
| `combat` | Optional D&D 5e stat block | `combat` |
| `hook` | "Sessão hoje. Zero NPCs prontos?" / "Session tonight. No NPCs ready?" | `roster` |

## Video assets (generated)

Four ~14.5 s cuts (5 scenes, cross-fades, Ken Burns motion), rendered from
the real app screenshots in [`docs/screenshots`](../screenshots), with the
soundtrack **"Games Worldbeat"** ([Mixkit](https://mixkit.co/free-stock-music/),
free for commercial use, no attribution required) normalized to -14 LUFS and
faded with the video. The raw track is gitignored (Mixkit forbids
redistributing it) — `generate.py` downloads it on demand.

| File | Size | Use |
|---|---|---|
| `video_{pt,en}_vertical.mp4` | 1080×1920 (9:16) | YouTube Shorts / vertical placements — highest App-campaign demand |
| `video_{pt,en}_square.mp4` | 1080×1080 (1:1) | Feeds / Display |
| `video_{pt,en}_landscape.mp4` | 1920×1080 (16:9) | In-stream / Display |

Storyboard (both languages follow the same beats):

| # | Time | Scene | On-screen text (pt / en) |
|---|---|---|---|
| 1 | 0.0–3.0 | Hook, text on gradient | "Sessão hoje. Zero NPCs prontos?" / "Session tonight. No NPCs ready?" |
| 2 | 2.5–6.0 | `randomize` screenshot | "Um toque. NPC completo." / "One tap. A full NPC." |
| 3 | 5.5–9.0 | `detail` screenshot | "Retrato de IA para cada NPC" / "An AI portrait for every NPC" |
| 4 | 8.5–11.5 | `combat` screenshot | "Ficha D&D 5e opcional" / "Optional D&D 5e stat block" |
| 5 | 11.0–14.5 | End card: icon + CTA | "Baixe no Google Play" / "Get it on Google Play" |

Before adding to a campaign:

1. Upload to the YouTube channel linked to the Google Ads account as
   **Unlisted** (Public also works; Private does not serve).
2. Add the YouTube URLs as video assets in the matching language campaign.

## HTML5 playable (generated)

`html5_pt.zip` / `html5_en.zip` (~14 KB each) — an interactive mini
"roll an NPC" demo in the app's visual identity: an NPC card (name, race,
profession, personality, motivation — with pt-BR gender agreement), a
re-roll button and a CTA that calls `ExitApi.exit()`. Upload the ZIP as an
**HTML5 asset** in the matching language campaign's ad group.

Meets the [App-campaign HTML5 spec](https://support.google.com/google-ads/answer/9981650):
single ZIP ≤5 MB with `index.html` at the root, everything inlined (no
external requests), `<meta name="ad.orientation" content="portrait,landscape">`,
responsive layout, Google's Exit API script wired to the CTA, no audio.
To preview locally: unzip and open `index.html` in a browser (the CTA is a
no-op outside the ads runtime).

## Regenerating

Same pipeline as the [Product Hunt kit](../producthunt): each scene is an
HTML/CSS card rendered with headless Chrome at 2×; ffmpeg animates the
cards (zoompan + xfade) into the MP4s and ImageMagick downscales the
static images.

```bash
python3 docs/ads/generate.py
```

Requires `google-chrome` (or `chromium`), ImageMagick and `ffmpeg` on `PATH`.
