#!/usr/bin/env python3
"""轻量 Java 语法结构检查：括号配对 + 常见错误"""
import os, re, glob

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "app/src/main/java")
ERRORS = []

def check_file(path):
    with open(path, encoding="utf-8") as f:
        src = f.read()

    rel = os.path.relpath(path, os.path.dirname(os.path.dirname(os.path.dirname(ROOT))))

    # 去掉注释和字符串，避免误判
    clean = re.sub(r'"[^"]*"', '""', src)
    clean = re.sub(r"'[^']*'", "''", clean)
    clean = re.sub(r'//[^\n]*', '', clean)
    clean = re.sub(r'/\*.*?\*/', '', clean, flags=re.S)

    # 括号配对
    pairs = [('(', ')'), ('{', '}'), ('[', ']')]
    for open_c, close_c in pairs:
        count = clean.count(open_c) - clean.count(close_c)
        if count != 0:
            ERRORS.append(f"{rel}: {'{'*0}{open_c}/{close_c} 配对错误 (差值 {count})")

    # 常见错误：switch 缺 break（警告）
    # 每行检查基本结构
    lines = src.split('\n')
    for i, line in enumerate(lines, 1):
        s = line.strip()
        # if/for/while/switch 后应有 {
        m = re.match(r'(if|for|while|switch)\s*\(.*\)\s*$', s)
        if m and i < len(lines):
            nxt = lines[i].strip()
            if nxt and not nxt.startswith('{') and not nxt.startswith('//') and not nxt.startswith('/*'):
                # 单行 if/for 是合法的，跳过简单情况
                if ';' not in s and ')' in s:
                    pass  # 允许单行体（下一行是语句）
        # 方法调用后分号（粗略）
    # 检查类/方法基本结构
    if not re.search(r'class\s+\w+', src):
        ERRORS.append(f"{rel}: 未找到 class 定义")
    if 'package ' not in src:
        ERRORS.append(f"{rel}: 缺少 package 声明")

for f in sorted(glob.glob(os.path.join(ROOT, "**/*.java"), recursive=True)):
    check_file(f)

print(f"Java 语法结构检查 | 文件: {len(glob.glob(os.path.join(ROOT,'**/*.java'), recursive=True))} | 错误: {len(ERRORS)}")
for e in ERRORS:
    print(f"  ❌ {e}")
