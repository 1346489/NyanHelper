#!/usr/bin/env python3
"""生成占位 PNG，用户上传真图后覆盖即可"""
from PIL import Image, ImageDraw
import os

D = "app/src/main/res/drawable"
os.makedirs(D, exist_ok=True)

def make(path, size, color, circle=False):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if circle:
        d.ellipse([0, 0, size-1, size-1], fill=color)
    else:
        d.rectangle([0, 0, size-1, size-1], fill=color)
    img.save(path)

make(f"{D}/ic_launcher.png", 512, (255, 192, 203, 255))       # 第1张占位（应用图标）
make(f"{D}/avatar_main.png", 512, (255, 220, 235, 255))       # 第5张占位（主界面头像）
make(f"{D}/icon_ball.png", 512, (255, 170, 200, 255), circle=True)  # 第2张占位（悬浮球）

print("占位图已生成：ic_launcher / avatar_main / icon_ball")
