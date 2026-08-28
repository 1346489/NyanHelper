#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
把 3 张原图切成全套图标尺寸。
用法（电脑/CI 均可）：
    python3 gen_icons.py

约定（对应你上传的原图）：
    raw/01_app_icon.png   -> 第1张: 应用图标 (ic_launcher)
    raw/02_ball_icon.png   -> 第2张: 悬浮球图标 (icon_ball, 会被裁成圆形)
    raw/05_main_avatar.png -> 第5张: 主界面头像 (avatar_main)
    raw/03_main_bg.png     -> 第3张: 主界面背景 (可选, 用作 bg_main 参考)

输出到 app/src/main/res/...
"""
import os, shutil

ROOT = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.join(ROOT, "raw")
RES = os.path.join(ROOT, "app", "src", "main", "res")

SIZES = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

try:
    from PIL import Image, ImageDraw, ImageOps
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

def make_placeholder(path, size, color):
    """没有原图时生成纯色占位 png"""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    if HAS_PIL:
        Image.new("RGBA", (size, size), color).save(path)
    else:
        with open(path, "wb") as f:
            f.write(b"")  # 极端兜底，CI 会装 Pillow

def fit_square_crop(img, size):
    """等比缩放到 size，居中裁成正方形（保证圆形悬浮球不拉伸）"""
    img = img.convert("RGBA")
    img = ImageOps.fit(img, (size, size), Image.LANCZOS, centering=(0.5, 0.4))
    return img

def process(src, dst_path, size, square=True):
    os.makedirs(os.path.dirname(dst_path), exist_ok=True)
    if src and os.path.isfile(src):
        img = Image.open(src)
        if square:
            img = fit_square_crop(img, size)
        else:
            img = img.convert("RGBA")
            img.thumbnail((size, size), Image.LANCZOS)
        img.save(dst_path)
    else:
        make_placeholder(dst_path, size, (255, 209, 220, 255))

def main():
    p1 = os.path.join(RAW, "01_app_icon.png")    # 第1张
    p2 = os.path.join(RAW, "02_ball_icon.png")    # 第2张
    p3 = os.path.join(RAW, "05_main_avatar.png")  # 第5张

    # 1) 应用图标: 全套 mipmap
    for folder, size in SIZES.items():
        process(p1, os.path.join(RES, folder, "ic_launcher.png"), size)

    # 2) 悬浮球 + 主界面头像: 单张高清（圆形/方形由 XML 控制）
    process(p2, os.path.join(RES, "drawable", "icon_ball.png"), 512)
    process(p3, os.path.join(RES, "drawable", "avatar_main.png"), 512)

    # 3) 第1张同时作为 drawable/ic_launcher（低版本兜底）
    process(p1, os.path.join(RES, "drawable", "ic_launcher.png"), 512)

    # 4) 占位：如果没有第3张背景图，生成一个粉色渐变占位
    bg_out = os.path.join(RES, "drawable", "bg_main.png")
    if not os.path.isfile(bg_out):
        os.makedirs(os.path.dirname(bg_out), exist_ok=True)
        if HAS_PIL:
            Image.new("RGBA", (1080, 1920), (255, 240, 246, 255)).save(bg_out)

    print("图标生成完成:")
    for f in ["drawable/icon_ball.png", "drawable/avatar_main.png",
              "drawable/ic_launcher.png", "drawable/bg_main.png"]:
        print("  -", f)
    for folder in SIZES:
        print("  -", os.path.join(folder, "ic_launcher.png"))

if __name__ == "__main__":
    main()
