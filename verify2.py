#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""直接硬编码绝对路径校验，避免 cwd 干扰"""
import os, re

ROOT = "/data/workspace/NyanHelper-2.0"
OK, ERR = [], []

def check(p, d):
    fp = os.path.join(ROOT, p)
    if os.path.exists(fp) and os.path.isfile(fp): OK.append(f"  ✅ {d}")
    else: ERR.append(f"  ❌ {d}  ({p})")

def grep(p, pat, d):
    fp = os.path.join(ROOT, p)
    if not os.path.exists(fp): ERR.append(f"  ❌ 文件不存在: {p}"); return
    if re.search(pat, open(fp, encoding="utf-8").read(), re.M): OK.append(f"  ✅ {d}")
    else: ERR.append(f"  ❌ {d}")

# 文件存在性
files = [
    "app/src/main/java/com/moe/nyanhelper/AppContext.java",
    "app/src/main/java/com/moe/nyanhelper/MainActivity.java",
    "app/src/main/java/com/moe/nyanhelper/FloatService.java",
    "app/src/main/java/com/moe/nyanhelper/EffectOverlay.java",
    "app/src/main/java/com/moe/nyanhelper/NyanConfig.java",
    "app/src/main/java/com/moe/nyanhelper/NyanAccessibilityService.java",
    "app/src/main/java/com/moe/nyanhelper/FeaturesActivity.java",
    "app/src/main/java/com/moe/nyanhelper/SettingsActivity.java",
    "app/src/main/java/com/moe/nyanhelper/ThemeActivity.java",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/res/layout/activity_main.xml",
    "app/src/main/res/layout/float_ball.xml",
    "app/src/main/res/layout/float_panel.xml",
    "app/src/main/res/layout/panel_features.xml",
    "app/src/main/res/layout/panel_settings.xml",
    "app/src/main/res/layout/panel_theme.xml",
    "app/src/main/res/layout/activity_features.xml",
    "app/src/main/res/layout/activity_settings.xml",
    "app/src/main/res/layout/activity_theme.xml",
    "app/src/main/res/xml/accessibility_config.xml",
    "app/src/main/res/values/themes.xml",
    "app/src/main/res/values/colors.xml",
    "app/src/main/res/values/strings.xml",
    "app/src/main/res/drawable/ball_circle.xml",
    "app/src/main/res/drawable/avatar_frame.xml",
    "app/src/main/res/drawable/card_bg.xml",
    "app/src/main/res/drawable/bg_main.xml",
    ".github/workflows/build.yml",
    "app/build.gradle",
    "build.gradle",
    "settings.gradle",
    "gradle.properties",
]
for f in files: check(f, os.path.basename(f))

# 关键配置
grep("app/build.gradle", r'versionName\s+"2\.0"', "版本 2.0")
grep("app/build.gradle", r'compileSdk\s+34', "compileSdk 34")
grep("AndroidManifest.xml", r'@drawable/ic_launcher', "图标=第1张图")
grep("activity_main.xml", r'@drawable/avatar_main', "主界面头像=第5张图")
grep("float_ball.xml", r'@drawable/icon_ball', "悬浮球=第2张图")
grep("float_ball.xml", r'centerCrop', "悬浮球 centerCrop")
grep("float_ball.xml", r'scaleType', "悬浮球 scaleType")
grep("float_panel.xml", r'功能', "面板-功能")
grep("float_panel.xml", r'设置', "面板-设置")
grep("float_panel.xml", r'主题', "面板-主题")
grep("panel_features.xml", r'swAddNya', "开关: 加喵")
grep("panel_features.xml", r'swReplaceYou', "开关: 你→主人")
grep("panel_features.xml", r'swReplaceMe', "开关: 我→本喵")
grep("panel_settings.xml", r'swSnow', "开关: 雪花")
grep("panel_settings.xml", r'swMeteor', "开关: 流星")
grep("NyanConfig.java", r'public static void setSnow', "雪花/流星互斥")
grep("panel_theme.xml", r'themePink', "主题-樱花粉")
grep("panel_theme.xml", r'themeGreen', "主题-薄荷绿")
grep("panel_theme.xml", r'themePurple', "主题-星空紫")
grep("NyanAccessibilityService.java", r'ACTION_SET_TEXT', "无障碍文字替换")
grep("NyanConfig.java", r'replace\("你"', "你→主人逻辑")
grep("NyanConfig.java", r'replace\("我"', "我→本喵逻辑")
grep("NyanConfig.java", r'appendNya', "结尾加喵逻辑")
grep(".github/workflows/build.yml", r'gradle-version:\s*8\.4', "CI Gradle 8.4")
grep("build.gradle", r'gradle:8\.3\.2', "AGP 8.3.2")
grep("settings.gradle", r'dependencyResolutionManagement', "settings 管仓库")
grep("FloatService.java", r'EffectOverlay', "悬浮球绑定特效层")
grep("EffectOverlay.java", r'startSnow', "雪花动态")
grep("EffectOverlay.java", r'startMeteor', "流星动态")

print(f"总计: {len(OK)} 通过, {len(ERR)} 错误\n")
for o in OK: print(o)
if ERR:
    print("\n--- 错误 ---")
    for e in ERR: print(e)
exit(1 if ERR else 0)
