#!/usr/bin/env python3
"""精确校验：Java 的 R.xxx.YYY 引用是否在 res/ 中有对应定义。"""
import re, os, glob

ROOT = "/data/workspace/BenmaoAssistant"
JAVA = f"{ROOT}/app/src/main/java"
RES = f"{ROOT}/app/src/main/res"

def read(p):
    with open(p, encoding="utf-8", errors="ignore") as f:
        return f.read()

# 收集 res 中的定义
xml_ids = set()          # @+id/...
layouts = set()          # res/layout/*.xml
drawables = set()        # res/drawable + res/mipmap
strings = set()          # <string name=...>

for f in glob.glob(f"{RES}/**/*.xml", recursive=True):
    src = read(f)
    rel = os.path.relpath(f, RES)
    xml_ids.update(re.findall(r'@\+id/(\w+)', src))
    if rel.startswith("layout/"):
        layouts.add(os.path.splitext(os.path.basename(f))[0])
    if rel.startswith("drawable/") or rel.startswith("mipmap/"):
        drawables.add(os.path.splitext(os.path.basename(f))[0])
    strings.update(re.findall(r'<string name="(\w+)"', src))

# drawable 目录里也可能有 png/xml 混合，用目录列表兜底
for d in ["drawable", "drawable-hdpi", "drawable-mdpi", "drawable-xhdpi", "mipmap", "mipmap-anydpi-v26"]:
    dd = f"{RES}/{d}"
    if os.path.isdir(dd):
        for f in os.listdir(dd):
            drawables.add(os.path.splitext(f)[0])

# Java 引用
r_ids = set(); r_layouts = set(); r_draws = set(); r_strs = set()
for f in glob.glob(f"{JAVA}/**/*.java", recursive=True):
    src = read(f)
    r_ids.update(re.findall(r'\bR\.id\.(\w+)', src))
    r_layouts.update(re.findall(r'\bR\.layout\.(\w+)', src))
    r_draws.update(re.findall(r'\bR\.drawable\.(\w+)', src))
    r_strs.update(re.findall(r'\bR\.string\.(\w+)', src))
    r_draws.update(re.findall(r'\bR\.mipmap\.(\w+)', src))

def check(name, refs, defs):
    missing = sorted(refs - defs)
    print(f"\n[{name}] 引用 {len(refs)} 个, 定义 {len(defs)} 个, 缺失 {len(missing)} 个")
    for m in missing:
        print(f"   ❌ {m}")
    return len(missing)

total = 0
total += check("R.id", r_ids, xml_ids)
total += check("R.layout", r_layouts, layouts)
total += check("R.drawable/mipmap", r_draws, drawables)
total += check("R.string", r_strs, strings)

print("\n" + "="*40)
if total == 0:
    print("✅ 所有 R 引用在 res/ 中均有对应定义")
else:
    print(f"⚠️ 共 {total} 处缺失，需修复")
exit(total)
