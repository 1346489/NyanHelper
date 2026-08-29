import re, glob, os

root = "app/src/main"
layouts = glob.glob(f"{root}/res/layout/*.xml")
java_files = glob.glob(f"{root}/java/**/*.java", recursive=True)

# 逐字符用正则，Python 的 re 对 UTF-8 处理最可靠
pattern = re.compile(r'android:id="@+id/([A-Za-z0-9_]+)"')

defined = set()
for f in layouts:
    text = open(f, encoding="utf-8").read()
    ids = pattern.findall(text)
    defined.update(ids)

print(f"=== 布局中定义的 id（共 {len(defined)} 个）===")
for i in sorted(defined):
    print(" ", i)

# Java 引用
ref_pattern = re.compile(r'R\.id\.([A-Za-z0-9_]+)')
refs = set()
for f in java_files:
    text = open(f, encoding="utf-8").read()
    refs.update(ref_pattern.findall(text))

print(f"\n=== Java 引用的 id（共 {len(refs)} 个）===")

missing = refs - defined
extra = defined - refs
print(f"\n缺失（引用了但没定义）: {len(missing)}")
for i in sorted(missing):
    print("  ❌", i)
print(f"\n未使用（定义了但没引用）: {len(extra)}")
for i in sorted(extra):
    print("  ·", i)

if not missing:
    print("\n✅ 所有 R.id 引用均已在布局中定义")
