# Product Hunt launch kit

Marketing assets for the app's [Product Hunt](https://www.producthunt.com/) launch,
plus the script that produces them. These are promo images only — nothing here
ships in the app.

## Gallery images (1270×760)

Upload them **in this order** — Product Hunt uses the **first** image as the
social preview when the link is shared:

| # | File | Slide |
|---|------|-------|
| 1 | `1_hero.png` | Name, tagline and two-phone hero — **the social preview** |
| 2 | `2_randomize.png` | Roll a complete NPC in one tap |
| 3 | `3_detail.png` | AI portrait for every character |
| 4 | `4_combat.png` | Optional D&D 5e stat block |
| 5 | `5_backup.png` | Back up & restore your roster |

`thumbnail.png` (240×240) is the product thumbnail (the app icon).

## Launch copy

- **Name:** RPG NPC Generator
- **Tagline:** Instant D&D NPCs with AI portraits, ready for your table
- **Tags:** Games · Android · Artificial Intelligence
- **Links:** this GitHub repo + [Google Play](https://play.google.com/store/apps/details?id=me.kerooker.rpgcharactergenerator); mark it as open source (AGPL-3.0).

The full description and maker first-comment are kept with the draft on Product Hunt.

## Regenerating

Each slide is an HTML/CSS card (phone screenshot on a themed background) rendered
with headless Chrome at 2× and downscaled for crisp text. Source images come from
[`docs/screenshots`](../screenshots). To rebuild after changing a screenshot or any
slide copy:

```bash
python3 docs/producthunt/generate.py
```

Requires `google-chrome` (or `chromium`) and ImageMagick's `convert` on `PATH`.
