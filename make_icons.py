#!/usr/bin/env python3
"""生成应用图标(图21)和悬浮球图标(图20)

优先从外部URL下载真实图片; 若网络受限, 自动用程序化生成的猫耳少女图作为占位,
保证项目始终可构建、图标文件始终存在。
"""
import urllib.request, os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))
IMG = os.path.join(HERE, "app/src/main/res")

BALL_URL = "http://yb.woa.com/BQDMZhYuVjE"   # 图20 悬浮球
APP_URL  = "http://yb.woa.com/AuQ893hoUpO"   # 图21 应用图标

def fetch(url, dst, retries=2):
    for i in range(retries + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=30) as r:
                data = r.read()
            if len(data) > 1000:
                with open(dst, "wb") as f:
                    f.write(data)
                print(f"  下载成功: {os.path.basename(dst)} ({len(data)} bytes)")
                return True
        except Exception as e:
            print(f"  下载失败(第{i+1}次) {url}: {e}")
    return False

def make_cat_girl(size, bg_color, filename):
    """程序化画一只可爱的猫耳少女(占位图)"""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse((0, 0, size, size), fill=bg_color)
    r = size / 108.0
    hair = (245, 183, 209, 255)
    d.ellipse((int(24*r), int(18*r), int(84*r), int(86*r)), fill=hair)
    d.polygon([(int(24*r),int(34*r)),(int(44*r),int(8*r)),(int(50*r),int(40*r))], fill=hair)
    d.polygon([(int(84*r),int(34*r)),(int(64*r),int(8*r)),(int(58*r),int(40*r))], fill=hair)
    face = (255, 224, 230, 255)
    d.ellipse((int(34*r), int(38*r), int(74*r), int(80*r)), fill=face)
    eye = (120, 80, 160, 255)
    d.ellipse((int(42*r),int(54*r),int(50*r),int(64*r)), fill=eye)
    d.ellipse((int(58*r),int(54*r),int(66*r),int(64*r)), fill=eye)
    blush = (255, 150, 170, 200)
    d.ellipse((int(36*r),int(64*r),int(44*r),int(72*r)), fill=blush)
    d.ellipse((int(64*r),int(64*r),int(72*r),int(72*r)), fill=blush)
    d.arc((int(48*r),int(66*r),int(60*r),int(76*r)), 0, 180, fill=(200,80,110,255), width=max(1,int(r)))
    bow = (255, 120, 160, 255)
    d.polygon([(int(70*r),int(40*r)),(int(86*r),int(32*r)),(int(82*r),int(48*r))], fill=bow)
    d.polygon([(int(70*r),int(40*r)),(int(86*r),int(48*r)),(int(82*r),int(32*r))], fill=bow)
    d.ellipse((int(68*r),int(36*r),int(74*r),int(44*r)), fill=(255,200,220,255))
    img.save(filename)
    return img

def to_circle(src, size):
    im = Image.open(src).convert("RGBA")
    w, h = im.size
    side = min(w, h)
    left, top = (w - side) // 2, (h - side) // 2
    im = im.crop((left, top, left + side, top + side)).resize((size, size), Image.LANCZOS)
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size, size), fill=255)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(im, (0, 0), mask)
    return out

# ---------- 悬浮球图标(图20) ----------
ball = os.path.join(HERE, "ball_raw.png")
if not fetch(BALL_URL, ball):
    print("  → 使用程序化占位图(悬浮球)")
    make_cat_girl(432, (255, 128, 169, 255), ball)

for density, size in [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96),
                       ("xxhdpi", 144), ("xxxhdpi", 192)]:
    d = os.path.join(IMG, f"drawable-{density}")
    os.makedirs(d, exist_ok=True)
    to_circle(ball, size).save(os.path.join(d, "benmao_ball.png"))
to_circle(ball, 96).save(os.path.join(IMG, "drawable/benmao_ball.png"))
print("✅ 悬浮球图标 (drawable-*/benmao_ball.png)")

# ---------- 应用图标(图21) foreground ----------
app = os.path.join(HERE, "app_raw.png")
if not fetch(APP_URL, app):
    print("  → 使用程序化占位图(应用图标)")
    make_cat_girl(432, (255, 128, 169, 255), app)

canvas = 432
im = Image.open(app).convert("RGBA")
w, h = im.size
side = min(w, h)
im = im.crop(((w - side) // 2, (h - side) // 2, (w + side) // 2, (h + side) // 2))
safe = int(canvas * 0.66)
im = im.resize((safe, safe), Image.LANCZOS)
fg = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
fg.paste(im, ((canvas - safe) // 2, (canvas - safe) // 2))
for density, scale in [("mdpi", 108), ("hdpi", 162), ("xhdpi", 216),
                       ("xxhdpi", 324), ("xxxhdpi", 432)]:
    d = os.path.join(IMG, f"mipmap-{density}")
    os.makedirs(d, exist_ok=True)
    fg.resize((scale, scale), Image.LANCZOS).save(
        os.path.join(d, "ic_launcher_foreground.png"))
print("✅ 应用图标 foreground (mipmap-*/ic_launcher_foreground.png)")
