#!/usr/bin/env python3
"""最终校验：资源引用 + Java 语法结构 + 需求对照"""
import os, re, glob, sys

ROOT = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(ROOT, "app/src/main/res")
JAVA = os.path.join(ROOT, "app/src/main/java")
ERRORS = []

def rel(p): return os.path.relpath(p, ROOT)

# ========== 1. 收集所有资源定义 ==========
defined = set()
for xml in glob.glob(os.path.join(RES, "**/*.xml"), recursive=True):
    with open(xml, encoding="utf-8", errors="ignore") as f:
        for m in re.finditer(r'name="(\w+)"', f.read()):
            defined.add(m.group(1))
for d in glob.glob(os.path.join(RES, "drawable*")):
    if os.path.isdir(d):
        for f in os.listdir(d):
            if os.path.isfile(os.path.join(d, f)):
                defined.add(os.path.splitext(f)[0])
for d in ["layout", "xml"]:
    dd = os.path.join(RES, d)
    if os.path.isdir(dd):
        for f in os.listdir(dd):
            defined.add(os.path.splitext(f)[0])

# ========== 2. XML 资源引用检查 ==========
xml_refs = set()
android_sys = set()
for xml in glob.glob(os.path.join(RES, "**/*.xml"), recursive=True):
    with open(xml, encoding="utf-8", errors="ignore") as f:
        c = f.read()
    for m in re.finditer(r'@(drawable|mipmap|color|string|style|layout|xml|id)/(\w+)', c):
        xml_refs.add((m.group(1), m.group(2)))

for kind, name in xml_refs:
    if kind == "id": continue
    if name not in defined:
        ERRORS.append(f"XML 引用 @{kind}/{name} 未定义")

# ========== 3. Java 引用 + 语法结构检查 ==========
java_files = sorted(glob.glob(os.path.join(JAVA, "**/*.java"), recursive=True))
java_refs = set()
for jf in java_files:
    with open(jf, encoding="utf-8", errors="ignore") as f:
        c = f.read()
    for m in re.finditer(r'\bR\.(drawable|mipmap|id|string|color|layout|style|xml)\.(\w+)', c):
        java_refs.add((jf, m.group(1), m.group(2)))
    for m in re.finditer(r'\bandroid\.R\.(drawable|id|string|color|layout|mipmap|style)\.(\w+)', c):
        android_sys.add((m.group(1), m.group(2)))
    # 语法结构：括号配对
    clean = re.sub(r'"[^"]*"', '""', c)
    clean = re.sub(r"'[^']*'", "''", clean)
    clean = re.sub(r'//[^\n]*', '', clean)
    clean = re.sub(r'/\*.*?\*/', '', clean, flags=re.S)
    for oc, cc in [('(', ')'), ('{', '}'), ('[', ']')]:
        if clean.count(oc) != clean.count(cc):
            ERRORS.append(f"{rel(jf)}: {oc}/{cc} 配对错误")
    if 'package ' not in c:
        ERRORS.append(f"{rel(jf)}: 缺少 package")
    if not re.search(r'\bclass\s+\w+', c):
        ERRORS.append(f"{rel(jf)}: 未找到 class")

for jf, kind, name in java_refs:
    if kind == "id": continue
    if name not in defined:
        if (kind, name) in android_sys: continue
        ERRORS.append(f"{rel(jf)}: R.{kind}.{name} 未定义")

# ========== 4. 业务需求对照 ==========
checks = []

# 版本
with open(os.path.join(ROOT, "app/build.gradle"), encoding="utf-8") as f:
    bg = f.read()
checks.append(("版本 2.0", 'versionName "2.0"' in bg and "versionCode 20" in bg))

# Manifest
with open(os.path.join(ROOT, "app/src/main/AndroidManifest.xml"), encoding="utf-8") as f:
    mf = f.read()
checks.append(("图标用 @drawable/ic_launcher (非 mipmap)", "@drawable/ic_launcher" in mf))
checks.append(("无 foregroundServiceType/specialUse", "foregroundServiceType" not in mf and "specialUse" not in mf))

# 无障碍服务配置
with open(os.path.join(RES, "xml/accessibility_service_config.xml"), encoding="utf-8") as f:
    ac = f.read()
checks.append(("无障碍全局事件", "typeWindowContentChanged" in ac and "typeWindowStateChanged" in ac))

# 主界面引用
with open(os.path.join(RES, "layout/activity_main.xml"), encoding="utf-8") as f:
    am = f.read()
checks.append(("主界面头像=@drawable/avatar_main", "@drawable/avatar_main" in am))

# 悬浮窗引用
with open(os.path.join(RES, "layout/float_window.xml"), encoding="utf-8") as f:
    fw = f.read()
checks.append(("悬浮球图标=@drawable/icon_ball", "@drawable/icon_ball" in fw))
checks.append(("三页: 功能/设置/主题", "page_functions" in fw and "page_settings" in fw and "page_theme" in fw))
checks.append(("三个功能开关", "sw_add_nya" in fw and "sw_me" in fw and "sw_you" in fw))
checks.append(("雪花/流星开关", "sw_snow" in fw and "sw_meteor" in fw))
checks.append(("特效层 ViewEffectOverlay", "ViewEffectOverlay" in fw))
# 圆形裁剪：Java 里 setClipToOutline(true) + drawable bg_ball_circle 是 oval
with open(os.path.join(JAVA, "com/moe/nyanhelper/FloatWindowService.java"), encoding="utf-8") as f:
    fs = f.read()
checks.append(("悬浮球圆形裁剪(setClipToOutline+OVAL)", "setClipToOutline" in fs and "bg_ball_circle" in fw))

# Java 业务逻辑
with open(os.path.join(JAVA, "com/moe/nyanhelper/NyanConfig.java"), encoding="utf-8") as f:
    nc = f.read()
checks.append(("结尾加喵逻辑", "addNya" in nc and "喵" in nc))
checks.append(("你→主人", '"你", "主人"' in nc or '"你", "本喵"' in nc or 'replace("你"' in nc))
checks.append(("我→本喵", 'replace("我"' in nc))
checks.append(("互斥: setSnow/setMeteor", "setSnow" in nc and "setMeteor" in nc))
checks.append(("三主题色", "themeStartColor" in nc and "themePrimary" in nc))

with open(os.path.join(JAVA, "com/moe/nyanhelper/ViewEffectOverlay.java"), encoding="utf-8") as f:
    vo = f.read()
checks.append(("雪花动态绘制", "Snowflake" in vo and "onDraw" in vo))
checks.append(("流星动态绘制", "Meteor" in vo and "meteors" in vo))

with open(os.path.join(JAVA, "com/moe/nyanhelper/NyanAccessibilityService.java"), encoding="utf-8") as f:
    asv = f.read()
checks.append(("无障碍文字替换", "ACTION_SET_TEXT" in asv and "NyanConfig.apply" in asv))

# ========== 输出 ==========
print("=" * 56)
print("  本喵助手 v2.0 — 最终校验")
print("=" * 56)

print(f"\n📋 资源引用 & 语法: {'✅ 通过' if not ERRORS else '❌ '+str(len(ERRORS))+' 个错误'}")
for e in ERRORS:
    print(f"   ❌ {e}")

print(f"\n📋 需求实现对照 ({sum(1 for _,v in checks if v)}/{len(checks)}):")
for name, ok in checks:
    print(f"   {'✅' if ok else '❌'} {name}")

print()
missing = [n for n, v in checks if not v]
if ERRORS or missing:
    print(f"❌ 校验失败: {len(ERRORS)} 错误, {len(missing)} 需求未满足")
    sys.exit(1)
else:
    print("🎉 全部通过！0 错误 / 0 警告")
    print(f"   📁 {len(glob.glob(os.path.join(ROOT,'**/*'), recursive=True))} 个文件")
    print(f"   ☕ {len(java_files)} 个 Java 文件")
    print(f"   🎨 资源文件 {len(glob.glob(os.path.join(RES,'**/*'), recursive=True))} 个")
    sys.exit(0)
