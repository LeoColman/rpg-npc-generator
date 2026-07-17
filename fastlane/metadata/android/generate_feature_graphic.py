#!/usr/bin/env python3
"""Regenerate the Google Play feature graphic for each store locale.

The feature graphic is the 1024x500 banner shown at the top of the Play
listing. Each locale's card is an HTML/CSS layout (brand + tagline + the
randomize screen phone on the app's themed background), rendered with
headless Chrome at 2x and downscaled for crisp text. It is written to
`fastlane/metadata/android/<locale>/images/featureGraphic.png`, from where
`fastlane upload_to_play_store` picks it up.

Inputs : docs/screenshots/randomize.png and the app launcher icon.
Outputs: featureGraphic.png under each locale's images/ dir.

Requires: google-chrome (or chromium) and ImageMagick on PATH.

Usage: python3 fastlane/metadata/android/generate_feature_graphic.py
"""
import base64
import pathlib
import shutil
import subprocess
import sys

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[2]
SHOT = ROOT / "docs" / "screenshots" / "randomize.png"
ICON = ROOT / "app" / "src" / "main" / "res" / "mipmap-xxxhdpi" / "ic_launcher.png"
BUILD = HERE / "_build"

W, H = 1024, 500

# Play may overlay a centered play button when a promo video is set, so the
# phone sits right-of-center and the copy stays left — center is kept clear.
COPY = {
    "en-US": {
        "name": "RPG NPC Generator",
        "tagline": 'Instant D&amp;D NPCs with<br><b>AI portraits</b> for your table',
        "chips": ("Free", "Open source", "Offline"),
    },
    "pt-BR": {
        "name": "Gerador de NPC para RPG",
        "tagline": 'NPCs de D&amp;D num toque<br>com <b>retrato de IA</b>',
        "chips": ("Grátis", "Open source", "Offline"),
    },
}

TPL = """<!doctype html><html><head><meta charset="utf-8"><style>
*{{margin:0;padding:0;box-sizing:border-box}}
html,body{{width:{w}px;height:{h}px;overflow:hidden}}
body{{font-family:"Liberation Sans","DejaVu Sans",Arial,sans-serif;
  -webkit-font-smoothing:antialiased;background:#0a0b14;color:#f2f4fb}}
.slide{{position:relative;width:{w}px;height:{h}px;overflow:hidden;
  background:
    radial-gradient(70% 90% at 90% 12%, rgba(120,132,255,.30), transparent 60%),
    radial-gradient(60% 80% at 4% 108%, rgba(196,90,220,.20), transparent 60%),
    linear-gradient(140deg,#15162c 0%,#0c0d1a 55%,#08090f 100%)}}
.slide:after{{content:"";position:absolute;inset:0;
  box-shadow:inset 0 0 200px rgba(0,0,0,.55);pointer-events:none}}
.wrap{{display:flex;height:100%;align-items:center}}
.left{{flex:1;padding:0 20px 0 68px;z-index:2}}
.brand{{display:flex;align-items:center;gap:15px;margin-bottom:28px}}
.brand img{{width:60px;height:60px;border-radius:15px;box-shadow:0 6px 18px rgba(0,0,0,.5)}}
.brand span{{font-size:30px;font-weight:800;letter-spacing:-.3px;color:#eef0fb}}
.tagline{{font-size:46px;font-weight:800;line-height:1.1;letter-spacing:-1px;color:#f6f7fe}}
.tagline b{{color:#aeb8ff}}
.chips{{display:flex;gap:11px;margin-top:30px}}
.chip{{font-size:19px;color:#c4cae0;border:1px solid rgba(154,166,255,.34);
  border-radius:999px;padding:9px 19px;background:rgba(120,132,255,.06)}}
.right{{width:400px;display:flex;align-items:flex-start;justify-content:center;
  position:relative;height:100%}}
.device{{position:absolute;top:64px;background:#04050a;padding:8px;border-radius:34px;
  transform:rotate(3deg);
  box-shadow:0 40px 80px rgba(0,0,0,.6),0 0 0 1px rgba(255,255,255,.06),0 0 0 7px rgba(120,132,255,.05)}}
.device img{{display:block;border-radius:27px;height:560px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="left">
  <div class="brand"><img src="{icon}"><span>{name}</span></div>
  <div class="tagline">{tagline}</div>
  <div class="chips">{chips}</div>
</div>
<div class="right"><div class="device"><img src="{shot}"></div></div>
</div></div></body></html>"""


def b64(path: pathlib.Path) -> str:
    return "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode()


def which(*names: str) -> str:
    for name in names:
        if shutil.which(name):
            return name
    sys.exit(f"None of {names} found on PATH.")


def main():
    chrome = which("google-chrome", "google-chrome-stable", "chromium", "chromium-browser")
    magick = which("magick", "convert")
    BUILD.mkdir(exist_ok=True)
    icon, shot = b64(ICON), b64(SHOT)

    for locale, c in COPY.items():
        html = TPL.format(w=W, h=H, icon=icon, shot=shot, name=c["name"],
                          tagline=c["tagline"],
                          chips="".join(f'<div class="chip">{i}</div>' for i in c["chips"]))
        page = BUILD / f"{locale}.html"
        page.write_text(html, encoding="utf-8")
        raw = BUILD / f"{locale}_2x.png"
        subprocess.run([
            chrome, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
            "--force-device-scale-factor=2", f"--window-size={W},{H}",
            "--virtual-time-budget=2000", f"--screenshot={raw}", page.as_uri(),
        ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        out = HERE / locale / "images" / "featureGraphic.png"
        out.parent.mkdir(parents=True, exist_ok=True)
        # Flatten to 24-bit (Play rejects alpha in the feature graphic).
        subprocess.run([magick, str(raw), "-resize", f"{W}x{H}",
                        "-background", "#08090f", "-alpha", "remove", "-alpha", "off",
                        "-strip", str(out)], check=True)
        print(f"rendered {out.relative_to(ROOT)}")

    shutil.rmtree(BUILD, ignore_errors=True)


if __name__ == "__main__":
    main()
