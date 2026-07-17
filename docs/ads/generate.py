#!/usr/bin/env python3
"""Regenerate the Google Ads asset kit (images + videos) from the app screenshots.

Renders static display images (1200x628, 1200x1200, 1200x1500) and four short
video drafts (9:16 1080x1920 and 16:9 1920x1080, in pt-BR and en-US). Each
scene is an HTML/CSS card rendered with headless Chrome at 2x; ffmpeg animates
the scene cards (zoompan + xfade) into ~14.5s MP4s and ImageMagick downscales
the static images for crisp text.

Inputs : docs/screenshots/*.png and the app launcher icon.
Outputs: the PNGs and MP4s next to this script (docs/ads/).

Requires: google-chrome (or chromium), ImageMagick and ffmpeg on PATH.

Usage: python3 docs/ads/generate.py
"""
import base64
import json
import pathlib
import shutil
import subprocess
import sys
import urllib.request
import zipfile

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SHOTS = ROOT / "docs" / "screenshots"
ICON = ROOT / "app" / "src" / "main" / "res" / "mipmap-xxxhdpi" / "ic_launcher.png"
BUILD = HERE / "_build"

# "Games Worldbeat" — Mixkit Stock Music License (free commercial use, no
# attribution). The license forbids redistributing the raw file, so it is
# gitignored and fetched on demand.
MUSIC = HERE / "music.mp3"
MUSIC_URL = "https://assets.mixkit.co/music/466/466.mp3"

FPS = 30
FADE = 0.5
ZOOM = 0.06


def b64(path: pathlib.Path) -> str:
    return "data:image/png;base64," + base64.b64encode(path.read_bytes()).decode()


def which(*names: str) -> str:
    for name in names:
        if shutil.which(name):
            return name
    sys.exit(f"None of {names} found on PATH.")


BASE_CSS = """
*{{margin:0;padding:0;box-sizing:border-box}}
html,body{{width:{w}px;height:{h}px;overflow:hidden}}
body{{font-family:"Liberation Sans","DejaVu Sans",Arial,sans-serif;
  -webkit-font-smoothing:antialiased;background:#0a0b14;color:#f2f4fb}}
.slide{{position:relative;width:{w}px;height:{h}px;overflow:hidden;
  background:
    radial-gradient(70% 55% at 88% 8%, rgba(120,132,255,.28), transparent 60%),
    radial-gradient(60% 55% at 6% 108%, rgba(196,90,220,.18), transparent 60%),
    linear-gradient(140deg,#15162c 0%,#0c0d1a 55%,#08090f 100%)}}
.slide:after{{content:"";position:absolute;inset:0;
  box-shadow:inset 0 0 240px rgba(0,0,0,.55);pointer-events:none}}
.brand{{display:flex;align-items:center;gap:16px}}
.brand img{{width:52px;height:52px;border-radius:13px;box-shadow:0 6px 18px rgba(0,0,0,.5)}}
.brand span{{font-size:25px;font-weight:700;letter-spacing:.2px;color:#eef0fb}}
.eyebrow{{font-weight:700;letter-spacing:3px;text-transform:uppercase;color:#9aa6ff}}
.chips{{display:flex;gap:12px;flex-wrap:wrap;justify-content:inherit}}
.chip{{font-size:20px;color:#c4cae0;border:1px solid rgba(154,166,255,.32);
  border-radius:999px;padding:10px 20px;background:rgba(120,132,255,.06)}}
.cta{{font-size:26px;font-weight:800;color:#0a0b14;background:#aeb8ff;border:none;
  border-radius:999px;padding:16px 34px;display:inline-block}}
.device{{position:relative;background:#04050a;padding:9px;border-radius:40px;
  box-shadow:0 45px 90px rgba(0,0,0,.62),0 0 0 1px rgba(255,255,255,.06),0 0 0 8px rgba(120,132,255,.05)}}
.device img{{display:block;border-radius:32px}}
b{{color:#aeb8ff}}
"""

# ---------------------------------------------------------------- static ads
IMAGE_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;align-items:center;flex-direction:{dir}}}
.left{{flex:1;padding:{pad};display:flex;flex-direction:column;justify-content:center}}
.brand{{margin-bottom:34px}}
.brand img{{width:64px;height:64px;border-radius:16px}}
.brand span{{font-size:32px;font-weight:800}}
.tagline{{font-size:{tag}px;font-weight:800;line-height:1.08;letter-spacing:-1px}}
.sub{{font-size:{sub}px;color:#aab2ca;margin-top:22px;line-height:1.4;max-width:90%}}
.chips{{margin-top:34px}}
.cta{{margin-top:28px;align-self:flex-start}}
.right{{display:flex;align-items:center;justify-content:center;{right}}}
.device{{transform:rotate({rot}deg)}}
.device img{{height:{shot}px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="left">
  <div class="brand"><img src="{icon}"><span>{name}</span></div>
  <div class="tagline">{tagline}</div>
  <div class="sub">{subtext}</div>
  <div class="chips">{chips}</div>
  <div class="cta">{cta}</div>
</div>
<div class="right"><div class="device"><img src="{shotimg}"></div></div>
</div></div></body></html>"""

# ---------------------------------------------------------------- video cards
HOOK_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;flex-direction:column;align-items:center;
  justify-content:center;text-align:center;padding:{pad}}}
.brand{{position:absolute;top:{brandtop}px;left:0;right:0;justify-content:center}}
.eyebrow{{font-size:{eye}px;margin-bottom:34px}}
.headline{{font-size:{head}px;font-weight:800;line-height:1.1;letter-spacing:-2px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="brand"><img src="{icon}"><span>{name}</span></div>
<div class="eyebrow">{eyebrow}</div>
<div class="headline">{headline}</div>
</div></div></body></html>"""

FEATURE_V_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;flex-direction:column;align-items:center;
  text-align:center;padding:90px 70px 80px}}
.headline{{font-size:82px;font-weight:800;line-height:1.08;letter-spacing:-1.5px}}
.sub{{font-size:37px;color:#aab2ca;line-height:1.4;margin-top:24px;max-width:880px}}
.shot{{flex:1;display:flex;align-items:center;justify-content:center;margin-top:40px}}
.device img{{height:1080px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="headline">{headline}</div>
<div class="sub">{subtext}</div>
<div class="shot"><div class="device"><img src="{shotimg}"></div></div>
</div></div></body></html>"""

FEATURE_S_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;flex-direction:column;align-items:center;
  text-align:center;padding:56px 60px 44px}}
.headline{{font-size:60px;font-weight:800;line-height:1.08;letter-spacing:-1px}}
.sub{{font-size:28px;color:#aab2ca;line-height:1.4;margin-top:16px;max-width:820px}}
.shot{{flex:1;display:flex;align-items:center;justify-content:center;margin-top:26px}}
.device img{{height:600px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="headline">{headline}</div>
<div class="sub">{subtext}</div>
<div class="shot"><div class="device"><img src="{shotimg}"></div></div>
</div></div></body></html>"""

FEATURE_H_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;align-items:center}}
.left{{flex:1;padding:0 40px 0 130px}}
.headline{{font-size:88px;font-weight:800;line-height:1.08;letter-spacing:-1.5px;max-width:900px}}
.sub{{font-size:37px;color:#aab2ca;line-height:1.45;margin-top:30px;max-width:820px}}
.right{{width:760px;display:flex;align-items:center;justify-content:center;transform:rotate(2.2deg)}}
.device img{{height:900px}}
</style></head><body><div class="slide"><div class="wrap">
<div class="left"><div class="headline">{headline}</div><div class="sub">{subtext}</div></div>
<div class="right"><div class="device"><img src="{shotimg}"></div></div>
</div></div></body></html>"""

END_TPL = """<!doctype html><html><head><meta charset="utf-8"><style>{css}
.wrap{{display:flex;height:100%;flex-direction:column;align-items:center;
  justify-content:center;text-align:center;padding:{pad}}}
.icon{{width:{iconsz}px;height:{iconsz}px;border-radius:{iconr}px;
  box-shadow:0 30px 70px rgba(0,0,0,.6)}}
.name{{font-size:{namesz}px;font-weight:800;letter-spacing:-1px;margin-top:44px}}
.tagline{{font-size:{tag}px;color:#aab2ca;margin-top:20px;line-height:1.4;max-width:85%}}
.chips{{margin-top:44px;justify-content:center}}
.cta{{margin-top:44px;font-size:34px;padding:22px 48px}}
</style></head><body><div class="slide"><div class="wrap">
<img class="icon" src="{icon}">
<div class="name">{name}</div>
<div class="tagline">{tagline}</div>
<div class="chips">{chips}</div>
<div class="cta">{cta}</div>
</div></div></body></html>"""


def chips(*items):
    return "".join(f'<div class="chip">{i}</div>' for i in items)


COPY = {
    "pt": {
        "name": "Gerador de NPC para RPG",
        "tagline": "NPCs instantâneos com <b>retrato de IA</b> para sua mesa",
        "sub": "Role um NPC completo em segundos — nome, raça, personalidade e retrato.",
        "chips": ("Grátis", "Open source", "Offline"),
        "cta": "Baixe no Google Play",
        "hook_eyebrow": "Para mestres de RPG",
        "hook": "Sessão hoje.<br><b>Zero NPCs prontos?</b>",
        "hook_sub": "Gere um NPC completo em um toque e salve todo o seu elenco.",
        "end_tagline": "NPCs completos com retrato de IA, na hora.",
        "scenes": [
            ("randomize", "Um toque.<br><b>NPC completo.</b>",
             "Nome, raça, profissão, tendência, motivação e personalidade — re-role qualquer campo."),
            ("detail", "Retrato de <b>IA</b> para cada NPC",
             "Um retrato de fantasia gerado a partir dos próprios traços do personagem."),
            ("combat", "Ficha <b>D&amp;D 5e</b> opcional",
             "Atributos, CA, PV e nível de desafio quando o NPC precisar lutar."),
        ],
    },
    "en": {
        "name": "RPG NPC Generator",
        "tagline": "Instant D&amp;D NPCs with <b>AI portraits</b> for your table",
        "sub": "Roll a complete NPC in seconds — name, race, personality and a portrait.",
        "chips": ("Free", "Open source", "Offline"),
        "cta": "Get it on Google Play",
        "hook_eyebrow": "For game masters",
        "hook": "Session tonight.<br><b>No NPCs ready?</b>",
        "hook_sub": "Roll a complete NPC in one tap and save your whole roster.",
        "end_tagline": "Complete NPCs with AI portraits, instantly.",
        "scenes": [
            ("randomize", "One tap.<br><b>A full NPC.</b>",
             "Name, race, profession, alignment, motivation and personality — re-roll any field."),
            ("detail", "An <b>AI portrait</b> for every NPC",
             "A fantasy portrait generated from the character's own traits."),
            ("combat", "Optional <b>D&amp;D 5e</b> stat block",
             "Ability scores, AC, HP and challenge rating when a fight breaks out."),
        ],
    },
}

IMAGE_SIZES = {
    "landscape": (1200, 628, dict(dir="row", pad="0 20px 0 80px", tag=52, sub=24,
                                  right="width:430px", rot=3, shot=520)),
    "square": (1200, 1200, dict(dir="row", pad="0 10px 0 76px", tag=58, sub=26,
                                right="width:470px", rot=3, shot=620)),
    "portrait": (1200, 1500, dict(dir="column", pad="90px 90px 10px", tag=64, sub=28,
                                  right="flex:1;padding-bottom:40px", rot=2, shot=700)),
}

# scene durations in seconds: hook, 3 features, end card
DURATIONS = [3.0, 3.5, 3.5, 3.0, 3.5]

# ------------------------------------------------------------ HTML5 playable
# Google App-campaign HTML5 spec: single .zip <=5MB with index.html at the
# root, everything inline (no external requests except Google-hosted libs),
# ad.orientation meta, and a CTA wired to ExitApi.exit().
PLAYABLE = {
    "pt": {
        "roll": "\U0001f3b2 Rolar outro NPC",
        "tap": "Seu NPC está pronto — role quantos quiser:",
        "labels": {"race": "Raça", "profession": "Profissão",
                   "trait": "Personalidade", "motivation": "Motivação"},
        "races": [["Humano", "Humana"], ["Elfo", "Elfa"], ["Anão", "Anã"],
                  "Halfling", ["Gnomo", "Gnoma"], "Meio-Orc", "Tiefling",
                  ["Draconato", "Draconata"], ["Meio-Elfo", "Meia-Elfa"]],
        "professions": [["Taverneiro", "Taverneira"], ["Ferreiro", "Ferreira"],
                        "Guarda da cidade", ["Mercador", "Mercadora"], "Bardo",
                        ["Curandeiro", "Curandeira"], ["Caçador", "Caçadora"],
                        "Escriba", "Alquimista", ["Pescador", "Pescadora"]],
        "traits": [["Desconfiado", "Desconfiada"], ["Generoso", "Generosa"],
                   "Arrogante", "Covarde", ["Curioso", "Curiosa"],
                   ["Supersticioso", "Supersticiosa"], ["Teimoso", "Teimosa"],
                   ["Sonhador", "Sonhadora"], ["Rabugento", "Rabugenta"], "Otimista"],
        "motivations": ["Pagar uma dívida antiga", "Proteger a família",
                        ["Ficar rico", "Ficar rica"], "Uma vingança silenciosa",
                        "Conhecer o mundo", "Esconder um segredo",
                        "Ganhar fama", "Servir ao templo"],
    },
    "en": {
        "roll": "\U0001f3b2 Roll another NPC",
        "tap": "Your NPC is ready — roll as many as you like:",
        "labels": {"race": "Race", "profession": "Profession",
                   "trait": "Personality", "motivation": "Motivation"},
        "races": ["Human", "Elf", "Dwarf", "Halfling", "Gnome", "Half-Orc",
                  "Tiefling", "Dragonborn", "Half-Elf"],
        "professions": ["Innkeeper", "Blacksmith", "City guard", "Merchant",
                        "Bard", "Healer", "Hunter", "Scribe", "Alchemist", "Fisher"],
        "traits": ["Suspicious", "Generous", "Arrogant", "Cowardly", "Curious",
                   "Superstitious", "Stubborn", "Dreamy", "Grumpy", "Optimistic"],
        "motivations": ["Repay an old debt", "Protect the family", "Get rich",
                        "A quiet revenge", "See the world", "Hide a secret",
                        "Earn fame", "Serve the temple"],
    },
}

# [name, gender] — gender picks the m/f form of pt-BR races/professions/traits
NPC_NAMES = [["Eirone Vanne", "m"], ["Jisan Aideh", "m"], ["Mara Duskwell", "f"],
             ["Tobbin Ashfoot", "m"], ["Serra Nightbloom", "f"],
             ["Korrin Blackbraid", "m"], ["Nyla Emberfall", "f"],
             ["Dorn Greyhollow", "m"], ["Petra Vinesong", "f"],
             ["Falrik Stonebrew", "m"], ["Isolde Wren", "f"],
             ["Garrick Thistledown", "m"]]

PLAYABLE_TPL = """<!DOCTYPE html>
<html lang="__LANG__">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
<meta name="ad.orientation" content="portrait,landscape">
<script type="text/javascript" src="https://tpc.googlesyndication.com/pagead/gadgets/html5/api/exitapi.js"></script>
<style>
*{margin:0;padding:0;box-sizing:border-box}
html,body{width:100%;height:100%;overflow:hidden}
body{font-family:"Liberation Sans","DejaVu Sans",Arial,sans-serif;color:#f2f4fb;
  -webkit-font-smoothing:antialiased;display:flex;align-items:center;justify-content:center;
  background:
    radial-gradient(70% 55% at 88% 8%, rgba(120,132,255,.28), transparent 60%),
    radial-gradient(60% 55% at 6% 108%, rgba(196,90,220,.18), transparent 60%),
    linear-gradient(140deg,#15162c 0%,#0c0d1a 55%,#08090f 100%)}
.wrap{width:min(92vw,460px);max-height:100vh;overflow:auto;padding:4vmin 0;
  display:flex;flex-direction:column;align-items:center;text-align:center;gap:2.4vmin}
.brand{display:flex;align-items:center;gap:10px}
.brand img{width:clamp(30px,8vmin,44px);height:clamp(30px,8vmin,44px);border-radius:22%;
  box-shadow:0 6px 18px rgba(0,0,0,.5)}
.brand span{font-size:clamp(16px,4.4vmin,22px);font-weight:700}
.tap{font-size:clamp(13px,3.6vmin,17px);color:#aab2ca}
.card{width:100%;background:rgba(120,132,255,.08);border:1px solid rgba(154,166,255,.28);
  border-radius:18px;padding:clamp(14px,4vmin,24px);transition:opacity .18s,transform .18s}
.card.rolling{opacity:0;transform:translateY(10px)}
.npc-name{font-size:clamp(22px,6.4vmin,34px);font-weight:800;letter-spacing:-.5px}
.row{display:flex;justify-content:space-between;gap:14px;margin-top:clamp(8px,2.6vmin,14px);
  font-size:clamp(13px,3.8vmin,17px)}
.row .k{color:#9aa6ff;font-weight:700;white-space:nowrap}
.row .v{color:#e7eafb;text-align:right}
.roll{font-size:clamp(15px,4.4vmin,19px);font-weight:700;color:#eef0fb;cursor:pointer;
  background:rgba(120,132,255,.14);border:1px solid rgba(154,166,255,.4);
  border-radius:999px;padding:clamp(10px,3vmin,14px) clamp(22px,6vmin,34px)}
.roll:active{transform:scale(.96)}
.cta{font-size:clamp(16px,4.8vmin,21px);font-weight:800;color:#0a0b14;cursor:pointer;
  background:#aeb8ff;border:none;border-radius:999px;
  padding:clamp(13px,3.6vmin,18px) clamp(28px,8vmin,44px);
  box-shadow:0 12px 34px rgba(120,132,255,.35)}
.cta:active{transform:scale(.96)}
.chips{display:flex;gap:8px;flex-wrap:wrap;justify-content:center}
.chip{font-size:clamp(11px,3vmin,13px);color:#c4cae0;border:1px solid rgba(154,166,255,.32);
  border-radius:999px;padding:5px 12px;background:rgba(120,132,255,.06)}
</style>
</head>
<body>
<div class="wrap">
  <div class="brand"><img src="__ICON__" alt=""><span>__NAME__</span></div>
  <div class="tap">__TAP__</div>
  <div class="card" id="card">
    <div class="npc-name" id="npc-name"></div>
    <div class="row"><span class="k">__L_RACE__</span><span class="v" id="race"></span></div>
    <div class="row"><span class="k">__L_PROF__</span><span class="v" id="profession"></span></div>
    <div class="row"><span class="k">__L_TRAIT__</span><span class="v" id="trait"></span></div>
    <div class="row"><span class="k">__L_MOT__</span><span class="v" id="motivation"></span></div>
  </div>
  <button class="roll" id="roll">__ROLL__</button>
  <button class="cta" id="cta">__CTA__</button>
  <div class="chips">__CHIPS__</div>
</div>
<script>
var DATA = __DATA__;
var last = {};
function pick(list, field) {
  var v = list[Math.floor(Math.random() * list.length)];
  if (list.length > 1) while (v === last[field]) v = list[Math.floor(Math.random() * list.length)];
  last[field] = v;
  return v;
}
function form(entry, gi) {
  return Array.isArray(entry) ? (entry[gi] || entry[0]) : entry;
}
function fill() {
  var name = pick(DATA.names, "name");
  var gi = name[1] === "f" ? 1 : 0;
  document.getElementById("npc-name").textContent = name[0];
  ["race", "profession", "trait", "motivation"].forEach(function (f) {
    document.getElementById(f).textContent = form(pick(DATA[f + "s"], f), gi);
  });
}
var card = document.getElementById("card");
document.getElementById("roll").addEventListener("click", function () {
  card.classList.add("rolling");
  setTimeout(function () { fill(); card.classList.remove("rolling"); }, 180);
});
document.getElementById("cta").addEventListener("click", function () {
  if (typeof ExitApi !== "undefined") ExitApi.exit();
});
fill();
</script>
</body>
</html>
"""


def playable_html(lang, icon_uri):
    c, p = COPY[lang], PLAYABLE[lang]
    data = {"names": NPC_NAMES, "races": p["races"], "professions": p["professions"],
            "traits": p["traits"], "motivations": p["motivations"]}
    html = PLAYABLE_TPL
    for token, value in {
        "__LANG__": lang, "__ICON__": icon_uri, "__NAME__": c["name"],
        "__TAP__": p["tap"], "__ROLL__": p["roll"], "__CTA__": c["cta"],
        "__L_RACE__": p["labels"]["race"], "__L_PROF__": p["labels"]["profession"],
        "__L_TRAIT__": p["labels"]["trait"], "__L_MOT__": p["labels"]["motivation"],
        "__CHIPS__": chips(*c["chips"]), "__DATA__": json.dumps(data, ensure_ascii=False),
    }.items():
        html = html.replace(token, value)
    return html


def render(chrome, name, html, w, h):
    page = BUILD / f"{name}.html"
    page.write_text(html, encoding="utf-8")
    out = BUILD / f"{name}.png"
    subprocess.run([
        chrome, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
        "--force-device-scale-factor=2", f"--window-size={w},{h}",
        "--virtual-time-budget=2000", f"--screenshot={out}", page.as_uri(),
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return out


def video_cards(lang, orient, icon, img):
    c = COPY[lang]
    w, h = {"vertical": (1080, 1920), "square": (1080, 1080), "landscape": (1920, 1080)}[orient]
    css = BASE_CSS.format(w=w, h=h)
    if orient == "vertical":
        hook = HOOK_TPL.format(css=css, icon=icon, name=c["name"], pad="0 80px",
                               brandtop=170, eye=30, head=124,
                               eyebrow=c["hook_eyebrow"], headline=c["hook"])
        feats = [FEATURE_V_TPL.format(css=css, headline=head, subtext=sub, shotimg=img[shot])
                 for shot, head, sub in c["scenes"]]
        end = END_TPL.format(css=css, icon=icon, name=c["name"], pad="0 80px",
                             iconsz=230, iconr=56, namesz=72, tag=40,
                             tagline=c["end_tagline"], chips=chips(*c["chips"]), cta=c["cta"])
    elif orient == "square":
        hook = HOOK_TPL.format(css=css, icon=icon, name=c["name"], pad="0 90px",
                               brandtop=80, eye=26, head=96,
                               eyebrow=c["hook_eyebrow"], headline=c["hook"])
        feats = [FEATURE_S_TPL.format(css=css, headline=head, subtext=sub, shotimg=img[shot])
                 for shot, head, sub in c["scenes"]]
        end = END_TPL.format(css=css, icon=icon, name=c["name"], pad="0 90px",
                             iconsz=160, iconr=40, namesz=58, tag=32,
                             tagline=c["end_tagline"], chips=chips(*c["chips"]), cta=c["cta"])
    else:
        hook = HOOK_TPL.format(css=css, icon=icon, name=c["name"], pad="0 200px",
                               brandtop=110, eye=28, head=110,
                               eyebrow=c["hook_eyebrow"], headline=c["hook"])
        feats = [FEATURE_H_TPL.format(css=css, headline=head, subtext=sub, shotimg=img[shot])
                 for shot, head, sub in c["scenes"]]
        end = END_TPL.format(css=css, icon=icon, name=c["name"], pad="0 200px",
                             iconsz=170, iconr=42, namesz=76, tag=36,
                             tagline=c["end_tagline"], chips=chips(*c["chips"]), cta=c["cta"])
    return [hook, *feats, end], w, h


def assemble(frames, w, h, out):
    """Ken Burns each still, cross-fade the scenes, fade to black at the end,
    and lay the soundtrack under it (normalized, faded with the video)."""
    args, filters = [], []
    for i, (frame, dur) in enumerate(zip(frames, DURATIONS)):
        n = round(dur * FPS)
        z = f"1+{ZOOM}*on/{n}" if i % 2 == 0 else f"{1 + ZOOM}-{ZOOM}*on/{n}"
        args += ["-i", str(frame)]
        filters.append(
            f"[{i}:v]zoompan=z='{z}':x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)'"
            f":d={n}:s={w}x{h}:fps={FPS}[v{i}]")
    prev, offset = "v0", 0.0
    for i, dur in enumerate(DURATIONS[:-1]):
        offset += dur - FADE
        nxt = f"x{i + 1}"
        filters.append(f"[{prev}][v{i + 1}]xfade=transition=fade:duration={FADE}:offset={offset}[{nxt}]")
        prev = nxt
    total = offset + DURATIONS[-1]
    filters.append(f"[{prev}]fade=t=out:st={total - 0.6}:d=0.6,format=yuv420p[v]")
    maps = ["-map", "[v]"]
    if MUSIC.exists():
        filters.append(
            f"[{len(frames)}:a]atrim=0:{total},loudnorm=I=-14:TP=-1.5:LRA=11,"
            f"aresample=48000,afade=t=in:st=0:d=0.4,afade=t=out:st={total - 0.8}:d=0.8[a]")
        args += ["-i", str(MUSIC)]
        maps += ["-map", "[a]", "-c:a", "aac", "-b:a", "128k"]
    subprocess.run([
        "ffmpeg", "-y", *args, "-filter_complex", ";".join(filters), *maps,
        "-r", str(FPS), "-c:v", "libx264", "-crf", "20", "-preset", "medium",
        "-movflags", "+faststart", str(out),
    ], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


def main():
    chrome = which("google-chrome", "google-chrome-stable", "chromium", "chromium-browser")
    magick = which("magick", "convert")
    BUILD.mkdir(exist_ok=True)
    if not MUSIC.exists():
        print(f"downloading soundtrack -> {MUSIC.name}")
        urllib.request.urlretrieve(MUSIC_URL, MUSIC)
    icon = b64(ICON)
    img = {n: b64(SHOTS / f"{n}.png") for n in ["randomize", "detail", "combat", "roster"]}
    subprocess.run([magick, str(ICON), "-resize", "128x128", "-strip",
                    str(BUILD / "icon_small.png")], check=True)
    icon_small = b64(BUILD / "icon_small.png")

    for lang in COPY:
        c = COPY[lang]
        themes = [("hero", c["tagline"], c["sub"], "randomize"),
                  *[(shot, head, sub, shot) for shot, head, sub in c["scenes"]],
                  ("hook", c["hook"], c["hook_sub"], "roster")]
        for theme, headline, subtext, shot in themes:
            for label, (w, h, opts) in IMAGE_SIZES.items():
                html = IMAGE_TPL.format(css=BASE_CSS.format(w=w, h=h), icon=icon,
                                        name=c["name"], tagline=headline, subtext=subtext,
                                        chips=chips(*c["chips"]), cta=c["cta"],
                                        shotimg=img[shot], **opts)
                shot2x = render(chrome, f"image_{lang}_{theme}_{label}", html, w, h)
                subprocess.run([magick, str(shot2x), "-resize", f"{w}x{h}",
                                str(HERE / f"image_{lang}_{theme}_{label}.png")], check=True)
                print(f"rendered image_{lang}_{theme}_{label}.png")

        for orient in ("vertical", "square", "landscape"):
            cards, w, h = video_cards(lang, orient, icon, img)
            frames = [render(chrome, f"{lang}_{orient}_{i}", html, w, h)
                      for i, html in enumerate(cards)]
            out = HERE / f"video_{lang}_{orient}.mp4"
            assemble(frames, w, h, out)
            print(f"rendered {out.name}")

        zip_path = HERE / f"html5_{lang}.zip"
        with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("index.html", playable_html(lang, icon_small))
        print(f"rendered {zip_path.name} ({zip_path.stat().st_size // 1024} KB)")

    shutil.rmtree(BUILD, ignore_errors=True)


if __name__ == "__main__":
    main()
