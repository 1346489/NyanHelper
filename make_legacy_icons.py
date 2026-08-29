#!/usr/bin/env python3
"""旧 API(<26) 每密度 mipmap: 粉色圆角背景 + 图21居中"""
import os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
IMG = os.path.join(HERE, "app/src/main/res")
app = os.path.join(HERE, "app_raw.png")
PINK = (255, 128, 169, 255)

def round_rect(size, radius, color):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    ImageDraw.Draw(img).rounded_rectangle((0, 0, size, size), radius, fill=color)
    return img

def make_legacy(src, size):
    bg = round_rect(size, int(size * 0.22), PINK)
    fg = Image.open(src).convert("RGBA")
    w, h = fg.size
    side = min(w, h)
    fg = fg.crop(((w - side) // 2, (h - side) // 2, (w + side) // 2, (h + side) // 2))
    fg = fg.resize((int(size * 0.72), int(size * 0.72)), Image.LANCZOS)
    bg.paste(fg, ((size - int(size * 0.72)) // 2, (size - int(size * 0.72)) // 2), fg)
    return bg

for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96),
                       ("xxhdpi", 144), ("xxxhdpi", 192)]:
    d = os.path.join(IMG, f"mipmap-{density}")
    os.makedirs(d, exist_ok=True)
    make_legacy(app, size).save(os.path.join(d, "ic_launcher.png"))
    make_legacy(app, size).save(os.path.join(d, "ic_launcher_round.png"))
print("✅ legacy 应用图标 (mipmap-*/ic_launcher[|_round].png)")
