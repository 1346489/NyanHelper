#!/usr/bin/env python3
"""生成占位图标 (本地测试用，实际用你上传的猫耳少女原图覆盖即可)"""
from PIL import Image, ImageDraw
import os

OUT = os.path.join(os.path.dirname(__file__), "app/src/main/res/drawable")

def make_icon(path, size=512):
    img = Image.new("RGBA", (size, size), (255, 240, 246, 255))
    d = ImageDraw.Draw(img)
    # 圆形脸
    margin = int(size * 0.1)
    d.ellipse([margin, margin, size - margin, size - margin], fill=(255, 209, 220, 255))
    # 猫耳
    ear = int(size * 0.18)
    d.polygon([(size//2 - ear, margin), (size//2 - ear//2, -ear//2), (size//2, margin + ear//2)], fill=(255, 105, 180, 255))
    d.polygon([(size//2 + ear, margin), (size//2 + ear//2, -ear//2), (size//2, margin + ear//2)], fill=(255, 105, 180, 255))
    # 眼睛
    eye_y = int(size * 0.42)
    for cx in [size//2 - ear, size//2 + ear]:
        d.ellipse([cx - ear//3, eye_y - ear//4, cx + ear//3, eye_y + ear//4], fill=(255, 255, 255, 255))
        d.ellipse([cx - ear//6, eye_y - ear//8, cx + ear//6, eye_y + ear//8], fill=(180, 27, 96, 255))
    # 腮红
    blush_y = int(size * 0.55)
    for cx in [size//2 - int(ear*1.2), size//2 + int(ear*1.2)]:
        d.ellipse([cx - ear//3, blush_y, cx + ear//3, blush_y + ear//3], fill=(255, 166, 201, 120))
    # 嘴
    mouth_y = int(size * 0.62)
    d.arc([size//2 - ear//2, mouth_y, size//2 + ear//2, mouth_y + ear//2], 0, 180, fill=(180, 27, 96, 255), width=max(2, size//64))
    img.save(path)
    print(f"created: {path}")

os.makedirs(OUT, exist_ok=True)
make_icon(os.path.join(OUT, "ic_launcher.png"))
make_icon(os.path.join(OUT, "nyan_avatar.png"))
print("占位图标已生成 (上传原图后覆盖即可)")
