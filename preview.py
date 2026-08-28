#!/usr/bin/env python3
"""生成预览图：占位图标 + 应用界面示意"""
from PIL import Image, ImageDraw, ImageFont
import os

OUT = "/data/workspace/NyanHelper-2.0/preview.png"

# 占位图标（和 gen_placeholder.py 一致）
def make_icon(size=512):
    img = Image.new("RGBA", (size, size), (255, 240, 246, 255))
    d = ImageDraw.Draw(img)
    margin = int(size * 0.1)
    d.ellipse([margin, margin, size - margin, size - margin], fill=(255, 209, 220, 255))
    ear = int(size * 0.18)
    d.polygon([(size//2 - ear, margin), (size//2 - ear//2, -ear//2), (size//2, margin + ear//2)], fill=(255, 105, 180, 255))
    d.polygon([(size//2 + ear, margin), (size//2 + ear//2, -ear//2), (size//2, margin + ear//2)], fill=(255, 105, 180, 255))
    eye_y = int(size * 0.42)
    for cx in [size//2 - ear, size//2 + ear]:
        d.ellipse([cx - ear//3, eye_y - ear//4, cx + ear//3, eye_y + ear//4], fill=(255, 255, 255, 255))
        d.ellipse([cx - ear//6, eye_y - ear//8, cx + ear//6, eye_y + ear//8], fill=(180, 27, 96, 255))
    blush_y = int(size * 0.55)
    for cx in [size//2 - int(ear*1.2), size//2 + int(ear*1.2)]:
        d.ellipse([cx - ear//3, blush_y, cx + ear//3, blush_y + ear//3], fill=(255, 166, 201, 120))
    mouth_y = int(size * 0.62)
    d.arc([size//2 - ear//2, mouth_y, size//2 + ear//2, mouth_y + ear//2], 0, 180, fill=(180, 27, 96, 255), width=max(2, size//64))
    return img

# 拼预览图
W, H = 1200, 800
canvas = Image.new("RGB", (W, H), (255, 240, 246))
d = ImageDraw.Draw(canvas)

try:
    font = ImageFont.truetype("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc", 28)
    font_big = ImageFont.truetype("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc", 48)
    font_title = ImageFont.truetype("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc", 64)
except:
    font = ImageFont.load_default()
    font_big = font
    font_title = font

# 标题
d.text((W//2 - 200, 30), "本喵助手 2.0 预览", fill=(180, 27, 96), font=font_title)

# 左侧：图标 (3 个密度)
d.text((80, 130), "应用图标", fill=(180, 27, 96), font=font_big)
icon = make_icon(512)
for i, scale in enumerate([192, 144, 96]):
    resized = icon.resize((scale, scale), Image.LANCZOS)
    y = 190 + i * (scale + 30)
    canvas.paste(resized, (80, y), resized)
    labels = ["xxxhdpi (192)", "xxhdpi (144)", "xhdpi (96)"]
    d.text((80 + scale + 20, y + scale//3), labels[i], fill=(100, 50, 80), font=font)

# 右侧：应用界面示意
d.text((550, 130), "应用界面", fill=(180, 27, 96), font=font_big)

# 手机框
phone_x, phone_y = 600, 190
phone_w, phone_h = 360, 560
d.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + phone_h], radius=30, fill=(255, 255, 255), outline=(230, 200, 215), width=3)

# 顶部栏
d.rounded_rectangle([phone_x, phone_y, phone_x + phone_w, phone_y + 50], radius=30, fill=(255, 209, 220))
d.text((phone_x + phone_w//2 - 40, phone_y + 12), "本喵助手", fill=(180, 27, 96), font=font)

# 头像
avatar = icon.resize((100, 100), Image.LANCZOS)
canvas.paste(avatar, (phone_x + phone_w//2 - 50, phone_y + 70), avatar)

# 标题文字
d.text((phone_x + phone_w//2 - 55, phone_y + 180), "本喵助手", fill=(233, 30, 140), font=font_big)
d.text((phone_x + phone_w//2 - 70, phone_y + 220), "让文字变得可爱喵~", fill=(150, 100, 120), font=font)

# 状态卡片
card_x = phone_x + 20
card_y = phone_y + 270
card_w = phone_w - 40
d.rounded_rectangle([card_x, card_y, card_x + card_w, card_y + 130], radius=12, fill=(255, 248, 252), outline=(230, 200, 215), width=1)
d.text((card_x + 15, card_y + 12), "❌ 悬浮窗权限未开启", fill=(80, 50, 70), font=font)
d.text((card_x + 15, card_y + 45), "❌ 无障碍服务未开启", fill=(80, 50, 70), font=font)
d.text((card_x + 15, card_y + 78), "⚪ 悬浮窗服务未运行", fill=(80, 50, 70), font=font)

# 按钮
btn_y = card_y + 155
d.rounded_rectangle([card_x, btn_y, card_x + card_w, btn_y + 45], radius=10, fill=(233, 30, 140))
d.text((card_x + card_w//2 - 55, btn_y + 10), "开启悬浮窗", fill=(255, 255, 255), font=font)

btn_y2 = btn_y + 60
d.rounded_rectangle([card_x, btn_y2, card_x + card_w, btn_y2 + 45], radius=10, fill=(255, 240, 246), outline=(233, 30, 140), width=2)
d.text((card_x + card_w//2 - 75, btn_y2 + 10), "去开启无障碍服务", fill=(233, 30, 140), font=font)

btn_y3 = btn_y2 + 60
d.rounded_rectangle([card_x, btn_y3, card_x + card_w, btn_y3 + 45], radius=10, fill=(245, 245, 245))
d.text((card_x + card_w//2 - 55, btn_y3 + 10), "刷新状态", fill=(150, 150, 150), font=font)

# 底部说明
d.text((W//2 - 350, H - 60), "✅ 60/60 校验通过  ·  AGP 7.4.2 + Gradle 7.6.4  ·  图标上传原图后自动替换", fill=(150, 100, 120), font=font)

canvas.save(OUT)
print(f"preview saved: {OUT}")
