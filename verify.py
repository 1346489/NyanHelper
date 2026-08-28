#!/usr/bin/env python3
"""校验本喵助手工程：引用一致性检查（不依赖 Android SDK）。"""
import os
import re
from collections import Counter

ROOT = os.path.dirname(os.path.abspath(__file__))
APP = os.path.join(ROOT, "app", "src", "main")
JAVA = os.path.join(APP, "java", "com", "moe", "nyanhelper")
RES = os.path.join(APP, "res")

java_files = []
for dirpath, _, files in os.walk(JAVA):
    for f in files:
        if f.endswith(".java"):
            java_files.append(os.path.join(dirpath, f))

layout_files = []
for dirpath, _, files in os.walk(os.path.join(RES, "layout")):
    for f in files:
        if f.endswith(".xml"):
            layout_files.append(os.path.join(dirpath, f))

# 1. 收集布局中定义的 id（逐行，稳健匹配）
defined_ids = set()
id_pattern = re.compile(r'android:id\s*=\s*"@\+id/(\w+)"')
for path in layout_files:
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            m = id_pattern.search(line)
            if m:
                defined_ids.add(m.group(1))

# 2. 收集 Java 中引用的 R.id.xxx
used_ids = set()
for path in java_files:
    with open(path, encoding="utf-8") as fh:
        for m in re.finditer(r'R\.id\.(\w+)', fh.read()):
            used_ids.add(m.group(1))

# 排除 android.R.id.content（系统 id，不是我们定义的）
used_ids.discard("content")

# 3. 收集 NyanConfig 定义的方法
with open(os.path.join(JAVA, "NyanConfig.java"), encoding="utf-8") as fh:
    nyan_src = fh.read()
defined_methods = set(re.findall(r'(?:public static\s+)?(?:void|boolean|int|String)\s+(\w+)\s*\(', nyan_src))

# 4. 收集调用 NyanConfig.xxx 的地方（排除常量）
used_nyan = set()
for path in java_files:
    with open(path, encoding="utf-8") as fh:
        for m in re.finditer(r'NyanConfig\.(\w+)', fh.read()):
            used_nyan.add(m.group(1))
constants = {"THEME_SAKURA", "THEME_MINT", "THEME_STARRY"}

# 5. drawable 资源名
drawable_files = os.listdir(os.path.join(RES, "drawable"))
drawable_names = set()
for name in drawable_files:
    if name.endswith((".xml", ".png", ".jpg", ".webp")):
        drawable_names.add(name.rsplit(".", 1)[0])

# 6. 检查
errors = []
warnings = []

missing_ids = used_ids - defined_ids
if missing_ids:
    errors.append(f"R.id 引用但布局未定义: {sorted(missing_ids)}")

for u in sorted(used_nyan):
    if u in constants:
        continue
    if u not in defined_methods:
        warnings.append(f"NyanConfig.{u} 调用但方法可能不存在")

referenced_drawables = set()
for path in layout_files + java_files:
    with open(path, encoding="utf-8") as fh:
        for m in re.finditer(r'@?drawable/(\w+)', fh.read()):
            referenced_drawables.add(m.group(1))
user_upload = {"ic_launcher", "avatar_main", "icon_ball"}
for d in user_upload:
    if d in referenced_drawables and d not in drawable_names:
        warnings.append(f"drawable/{d}.(png/xml) 需上传或由占位提供")
missing_drawables = referenced_drawables - drawable_names - user_upload
if missing_drawables:
    errors.append(f"@drawable 引用但资源不存在: {sorted(missing_drawables)}")

if not os.path.exists(os.path.join(JAVA, "FloatWindowService.java")):
    errors.append("缺少 FloatWindowService.java")
if not os.path.exists(os.path.join(JAVA, "SnowMeteorView.java")):
    errors.append("缺少 SnowMeteorView.java")

# 检查 bg_float_panel 是否在 drawable 里
if "bg_float_panel" not in drawable_names:
    errors.append("缺少 drawable/bg_float_panel.xml")

print("=" * 50)
print("校验结果")
print("=" * 50)
print(f"Java 源文件: {len(java_files)}")
print(f"布局文件: {len(layout_files)}")
print(f"已定义 id ({len(defined_ids)}): {sorted(defined_ids)}")
print(f"已引用 id ({len(used_ids)}): {sorted(used_ids)}")
print(f"drawable 资源: {len(drawable_names)}")
print("-" * 50)
print(f"警告 ({len(warnings)}):")
for w in warnings:
    print(f"  ⚠ {w}")
print(f"错误 ({len(errors)}):")
for e in errors:
    print(f"  ✗ {e}")

if not errors:
    print("\n✅ 全部通过，无引用缺失错误")
    raise SystemExit(0)
else:
    raise SystemExit(1)
