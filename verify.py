#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""校验本喵助手 2.0 工程完整性（以脚本所在目录为根）"""
import os, re

ROOT = os.path.dirname(os.path.abspath(__file__))
ERRORS = []
OK = []

def rel(path):
    return os.path.join(ROOT, path)

def check(path, desc):
    full = rel(path)
    if os.path.exists(full):
        OK.append(f"  ✅ {desc}")
        return True
    ERRORS.append(f"  ❌ 缺失: {path}")
    return False

def grep(path, pattern, desc):
    full = rel(path)
    if not os.path.exists(full):
        ERRORS.append(f"  ❌ 无法检查 {path} ({desc})")
        return
    text = open(full, encoding="utf-8").read()
    if re.search(pattern, text, re.MULTILINE):
        OK.append(f"  ✅ {desc}")
    else:
        ERRORS.append(f"  ❌ {desc}")

print("=== 核心 Java 类 ===")
for f, d in [
    ("app/src/main/java/com/moe/nyanhelper/AppContext.java", "AppContext"),
    ("app/src/main/java/com/moe/nyanhelper/MainActivity.java", "MainActivity"),
    ("app/src/main/java/com/moe/nyanhelper/FloatService.java", "FloatService"),
    ("app/src/main/java/com/moe/nyanhelper/EffectOverlay.java", "EffectOverlay"),
    ("app/src/main/java/com/moe/nyanhelper/NyanConfig.java", "NyanConfig"),
    ("app/src/main/java/com/moe/nyanhelper/NyanAccessibilityService.java", "NyanAccessibilityService"),
    ("app/src/main/java/com/moe/nyanhelper/FeaturesActivity.java", "FeaturesActivity"),
    ("app/src/main/java/com/moe/nyanhelper/SettingsActivity.java", "SettingsActivity"),
    ("app/src/main/java/com/moe/nyanhelper/ThemeActivity.java", "ThemeActivity"),
]:
    check(f, d)

print("\n=== 图片资源映射（png 由你上传，脚本只检查占位 xml 是否存在）===")
check("app/src/main/res/drawable/ic_launcher.xml", "①应用图标占位 (替换为第1张 → ic_launcher.png)")
check("app/src/main/res/drawable/avatar_main.xml", "③主界面头像占位 (替换为第5张 → avatar_main.png)")
check("app/src/main/res/drawable/icon_ball.xml", "②悬浮球图标占位 (替换为第2张 → icon_ball.png)")

print("\n=== 资源 / 布局 ===")
for f, d in [
    ("app/src/main/AndroidManifest.xml", "AndroidManifest"),
    ("app/src/main/res/layout/activity_main.xml", "主界面 (像第4张图)"),
    ("app/src/main/res/layout/float_ball.xml", "悬浮球 (圆形)"),
    ("app/src/main/res/layout/float_panel.xml", "三页面板 (功能/设置/主题)"),
    ("app/src/main/res/layout/panel_features.xml", "功能页 (3开关)"),
    ("app/src/main/res/layout/panel_settings.xml", "设置页 (雪花/流星)"),
    ("app/src/main/res/layout/panel_theme.xml", "主题页 (3色)"),
    ("app/src/main/res/layout/activity_features.xml", "功能 Activity"),
    ("app/src/main/res/layout/activity_settings.xml", "设置 Activity"),
    ("app/src/main/res/layout/activity_theme.xml", "主题 Activity"),
    ("app/src/main/res/xml/accessibility_config.xml", "无障碍配置"),
    ("app/src/main/res/values/themes.xml", "themes.xml"),
    ("app/src/main/res/values/colors.xml", "colors.xml"),
    ("app/src/main/res/values/strings.xml", "strings.xml"),
    (".github/workflows/build.yml", "CI workflow"),
    ("app/build.gradle", "app/build.gradle"),
    ("build.gradle", "根 build.gradle"),
    ("settings.gradle", "settings.gradle"),
    ("gradle.properties", "gradle.properties"),
]:
    check(f, d)

print("\n=== 关键配置检查 ===")
grep("app/build.gradle", r'versionName\s+"2\.0"', "版本 2.0")
grep("app/build.gradle", r'compileSdk\s+34', "compileSdk 34")
grep("AndroidManifest.xml", r'@drawable/ic_launcher', "图标=第1张图")
grep("AndroidManifest.xml", r'application\s*:', "application 声明")
grep("activity_main.xml", r'@drawable/avatar_main', "主界面头像=第5张图")
grep("float_ball.xml", r'@drawable/icon_ball', "悬浮球=第2张图")
grep("float_ball.xml", r'centerCrop', "悬浮球图片完美覆盖")
grep("float_ball.xml", r'ball_circle|oval|scaleType', "悬浮球裁圆")
grep("float_panel.xml", r'功能', "面板-功能页")
grep("float_panel.xml", r'设置', "面板-设置页")
grep("float_panel.xml", r'主题', "面板-主题页")
grep("panel_features.xml", r'swAddNya', "功能: 结尾加喵")
grep("panel_features.xml", r'swReplaceYou', "功能: 你→主人")
grep("panel_features.xml", r'swReplaceMe', "功能: 我→本喵")
grep("panel_settings.xml", r'swSnow', "设置: 雪花")
grep("panel_settings.xml", r'swMeteor', "设置: 流星")
grep("NyanConfig.java", r'public static void setSnow', "雪花/流星互斥逻辑")
grep("panel_theme.xml", r'themePink', "主题: 樱花粉")
grep("panel_theme.xml", r'themeGreen', "主题: 薄荷绿")
grep("panel_theme.xml", r'themePurple', "主题: 星空紫")
grep("NyanAccessibilityService.java", r'ACTION_SET_TEXT', "无障碍全局文字替换")
grep(".github/workflows/build.yml", r'gradle-version:\s*8\.4', "CI Gradle 8.4")
grep("build.gradle", r'com\.android\.tools\.build:gradle:8\.3\.2', "AGP 8.3.2")
grep("settings.gradle", r'dependencyResolutionManagement', "settings 管理仓库")
grep("NyanConfig.java", r'isAddNya|appendNya', "结尾加喵逻辑")
grep("NyanConfig.java", r'replace\("你"', "你→主人")
grep("NyanConfig.java", r'replace\("我"', "我→本喵")

print(f"\n=== 结果 ===")
for o in OK: print(o)
for e in ERRORS: print(e)
print(f"\n总计: {len(OK)} 通过, {len(ERRORS)} 错误")
if ERRORS:
    print("\n注意: drawable/*.png 是占位 xml，你需要上传 3 张原图后删除同名 .xml")
exit(1 if ERRORS else 0)
