#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""最终校验：固定 cwd + 绝对路径"""
import os, re, sys

os.chdir("/data/workspace/NyanHelper-2.0")
ROOT = "/data/workspace/NyanHelper-2.0"
OK, ERR = [], []

def check(p, d):
    fp = os.path.join(ROOT, p)
    (OK if os.path.isfile(fp) else ERR).append(
        f"  {'✅' if os.path.isfile(fp) else '❌'} {d}  [{p}]")

def grep(p, pat, d):
    fp = os.path.join(ROOT, p)
    if not os.path.isfile(fp):
        ERR.append(f"  ❌ 文件不存在 {p}")
        return
    if re.search(pat, open(fp, encoding="utf-8").read(), re.M):
        OK.append(f"  ✅ {d}")
    else:
        ERR.append(f"  ❌ {d}")

# 文件清单（相对于工程根）
for p in [
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
    "verify.py",
]:
    check(p, os.path.basename(p).replace(".", "_"))

# 内容检查
grep("app/build.gradle", r'versionName\s+"2\.0"', "版本 2.0")
grep("app/build.gradle", r'compileSdk\s+34', "compileSdk 34")
grep("AndroidManifest.xml", r'@drawable/ic_launcher', "图标=第1张图")
grep("AndroidManifest.xml", r'android:name="\.AppContext"', "Application 声明")
grep("activity_main.xml", r'@drawable/avatar_main', "主界面头像=第5张图")
grep("activity_main.xml", r'btnFeatures.*功能|功能', "主界面-功能入口")
grep("float_ball.xml", r'@drawable/icon_ball', "悬浮球=第2张图")
grep("float_ball.xml", r'centerCrop', "悬浮球 centerCrop(完美覆盖)")
grep("float_ball.xml", r'oval|ball_circle', "悬浮球裁圆")
grep("float_panel.xml", r'功能', "面板-功能页")
grep("float_panel.xml", r'设置', "面板-设置页")
grep("float_panel.xml", r'主题', "面板-主题页")
grep("panel_features.xml", r'swAddNya', "功能: 结尾加喵")
grep("panel_features.xml", r'swReplaceYou', "功能: 你→主人")
grep("panel_features.xml", r'swReplaceMe', "功能: 我→本喵")
grep("panel_settings.xml", r'swSnow', "设置: 雪花(左上)")
grep("panel_settings.xml", r'swMeteor', "设置: 流星(右上)")
grep("NyanConfig.java", r'public static void setSnow', "雪花/流星互斥")
grep("NyanConfig.java", r'isSnow', "雪花状态读取")
grep("NyanConfig.java", r'isMeteor', "流星状态读取")
grep("panel_theme.xml", r'themePink', "主题-樱花粉")
grep("panel_theme.xml", r'themeGreen', "主题-薄荷绿")
grep("panel_theme.xml", r'themePurple', "主题-星空紫")
grep("NyanConfig.java", r'getThemeColor', "主题色取值")
grep("NyanAccessibilityService.java", r'ACTION_SET_TEXT', "无障碍文字替换")
grep("NyanConfig.java", r'replace\("你"', "你→主人 规则")
grep("NyanConfig.java", r'replace\("我"', "我→本喵 规则")
grep("NyanConfig.java", r'appendNya', "结尾加喵 规则")
grep("EffectOverlay.java", r'startSnow', "雪花动画")
grep("EffectOverlay.java", r'startMeteor', "流星动画")
grep("EffectOverlay.java", r'postDelayed', "动画循环")
grep(".github/workflows/build.yml", r'gradle-version:\s*8\.4', "CI Gradle 8.4")
grep("build.gradle", r'gradle:8\.3\.2', "AGP 8.3.2")
grep("settings.gradle", r'dependencyResolutionManagement', "settings 管仓库")
grep("MainActivity.java", r'FeaturesActivity', "主界面跳转功能")
grep("MainActivity.java", r'SettingsActivity', "主界面跳转设置")
grep("MainActivity.java", r'ThemeActivity', "主界面跳转主题")
grep("FloatService.java", r'EffectOverlay', "悬浮球绑定特效")
grep("AndroidManifest.xml", r'NyanAccessibilityService', "无障碍服务注册")
grep("AndroidManifest.xml", r'FloatService', "悬浮服务注册")

print(f"总计: {len(OK)} 通过, {len(ERR)} 错误\n")
for o in OK: print(o)
if ERR:
    print("\n--- 错误 ---")
    for e in ERR: print(e)
sys.exit(1 if ERR else 0)
