#!/usr/bin/env python3
"""校验 NyanHelper-v3 工程完整性"""
import os, re, glob

ROOT = os.path.dirname(os.path.abspath(__file__))
RES = os.path.join(ROOT, "app/src/main/res")
ERRORS = []
WARNINGS = []

def rel(path):
    return os.path.relpath(path, ROOT)

# 1. 收集所有 drawable/mipmap/color/string 定义
defined = set()
for xml in glob.glob(os.path.join(RES, "**/*.xml"), recursive=True):
    with open(xml, encoding="utf-8", errors="ignore") as f:
        content = f.read()
    for m in re.finditer(r'name="(\w+)"', content):
        defined.add(m.group(1))
# drawable = 文件名（不含扩展）
for d in glob.glob(os.path.join(RES, "drawable*", "*")):
    if os.path.isfile(d):
        defined.add(os.path.splitext(os.path.basename(d))[0])
for d in glob.glob(os.path.join(RES, "mipmap*", "*")):
    if os.path.isfile(d):
        defined.add(os.path.splitext(os.path.basename(d))[0])

# 2. 扫描所有引用 @drawable/ @mipmap/ @color/ @string/
# 同时收集 layout/ xml 目录资源（layout 名 = 文件名）
for d in glob.glob(os.path.join(RES, "layout*")):
    if os.path.isdir(d):
        for f in os.listdir(d):
            defined.add(os.path.splitext(f)[0])
for d in glob.glob(os.path.join(RES, "xml")):
    if os.path.isdir(d):
        for f in os.listdir(d):
            defined.add(os.path.splitext(f)[0])

refs = set()
for xml in glob.glob(os.path.join(RES, "**/*.xml"), recursive=True):
    with open(xml, encoding="utf-8", errors="ignore") as f:
        content = f.read()
    for m in re.finditer(r'@(drawable|mipmap|color|string|style|layout|xml)/(\w+)', content):
        refs.add((m.group(1), m.group(2)))

# Java 里 android.R.drawable / android.R.id 等系统资源（编译时由 SDK 提供，无需项目定义）
android_sys_refs = set()

# 跳过系统/不需要定义的
system_styles = {"Theme.NyanHelper", "Theme.MaterialComponents.Light.NoActionBar"}
for kind, name in sorted(refs):
    key = name
    if kind in ("drawable", "mipmap", "color", "string", "layout", "xml"):
        if name not in defined and f"{kind}/{name}" not in str(system_styles):
            # layout 引用在 layout 目录
            if kind == "layout" and os.path.exists(os.path.join(RES, "layout", f"{name}.xml")):
                continue
            if kind == "xml" and os.path.exists(os.path.join(RES, "xml", f"{name}.xml")):
                continue
            ERRORS.append(f"引用 @{kind}/{name} 未找到定义")

# 3. 检查同名 png/xml 冲突
for d in glob.glob(os.path.join(RES, "drawable*")):
    if not os.path.isdir(d): continue
    names = {}
    for f in os.listdir(d):
        base, ext = os.path.splitext(f)
        if ext.lower() in (".png", ".jpg", ".xml"):
            names.setdefault(base, []).append(ext.lower())
    for base, exts in names.items():
        if ".png" in exts and ".xml" in exts:
            ERRORS.append(f"同名冲突 {os.path.relpath(d,RES)}/{base}.png + {base}.xml")

# 4. 检查 Java 引用
java_refs = set()
for java in glob.glob(os.path.join(ROOT, "app/src/main/java/**/*.java"), recursive=True):
    with open(java, encoding="utf-8", errors="ignore") as f:
        content = f.read()
    for m in re.finditer(r'R\.(drawable|mipmap|id|string|color|layout)\.(\w+)', content):
        java_refs.add((m.group(1), m.group(2)))
    # 系统资源 android.R.drawable / android.R.id / android.R.string
    for m in re.finditer(r'android\.R\.(drawable|id|string|color|layout|mipmap)\.(\w+)', content):
        android_sys_refs.add((m.group(1), m.group(2)))

for kind, name in sorted(java_refs):
    if name not in defined:
        # id 定义在 layout 内，合法
        if kind == "id":
            continue
        # layout/xml 名 = 目录下的文件名
        if kind in ("layout", "xml"):
            continue
        # style 定义在 styles.xml 的 name=，已收集
        if kind == "style":
            continue
        # Android 系统资源（android.R.drawable.xxx 等），编译时由 SDK 提供
        if name in {n for _, n in android_sys_refs}:
            continue
        ERRORS.append(f"Java 引用 R.{kind}.{name} 未找到")

if android_sys_refs:
    print(f"\nℹ️ Java 使用的 Android 系统资源 ({len(android_sys_refs)} 个，正常):")
    for k, n in sorted(android_sys_refs):
        print(f"   android.R.{k}.{n}")

# 5. 检查 build.gradle 版本
bg = os.path.join(ROOT, "app/build.gradle")
with open(bg, encoding="utf-8") as f:
    bgc = f.read()
if "versionName \"3.0\"" not in bgc:
    ERRORS.append("versionName 不是 3.0")

# 6. Manifest 图标引用
manifest = os.path.join(ROOT, "app/src/main/AndroidManifest.xml")
with open(manifest, encoding="utf-8") as f:
    mc = f.read()
if "@drawable/ic_launcher" not in mc:
    ERRORS.append("Manifest 未引用 @drawable/ic_launcher")
if "specialUse" in mc or "foregroundServiceType" in mc:
    ERRORS.append("Manifest 仍含 foregroundServiceType/specialUse")

print("=" * 50)
print(f"校验完成 | 错误: {len(ERRORS)} | 警告: {len(WARNINGS)}")
print("=" * 50)
for e in ERRORS:
    print(f"  ❌ {e}")
for w in WARNINGS:
    print(f"  ⚠️ {w}")

# 文件清单
print("\n📁 文件清单:")
for f in sorted(glob.glob(os.path.join(ROOT, "**/*"), recursive=True)):
    if os.path.isfile(f):
        print(f"  {rel(f)}")

if android_sys_refs:
    print(f"\nℹ️ Java 使用 Android 系统资源 ({len(android_sys_refs)} 个，编译时由 SDK 提供，正常):")
    for k, n in sorted(android_sys_refs):
        print(f"   android.R.{k}.{n}")

exit(1 if ERRORS else 0)
