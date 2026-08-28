#!/bin/bash
# 最终核验：用系统 ls/grep，不依赖 Python
set -e
cd /data/workspace/NyanHelper-2.0
OK=0; ERR=0

check_file() {
    if [ -f "$1" ]; then
        echo "  ✅ $2"; OK=$((OK+1))
    else
        echo "  ❌ $2  ($1)"; ERR=$((ERR+1))
    fi
}

check_content() {
    if grep -qE "$1" "$2" 2>/dev/null; then
        echo "  ✅ $3"; OK=$((OK+1))
    else
        echo "  ❌ $3  (in $2)"; ERR=$((ERR+1))
    fi
}

echo "=== 文件存在性 ==="
check_file app/src/main/java/com/moe/nyanhelper/AppContext.java "AppContext.java"
check_file app/src/main/java/com/moe/nyanhelper/MainActivity.java "MainActivity.java"
check_file app/src/main/java/com/moe/nyanhelper/FloatService.java "FloatService.java"
check_file app/src/main/java/com/moe/nyanhelper/EffectOverlay.java "EffectOverlay.java"
check_file app/src/main/java/com/moe/nyanhelper/NyanConfig.java "NyanConfig.java"
check_file app/src/main/java/com/moe/nyanhelper/NyanAccessibilityService.java "NyanAccessibilityService.java"
check_file app/src/main/java/com/moe/nyanhelper/FeaturesActivity.java "FeaturesActivity.java"
check_file app/src/main/java/com/moe/nyanhelper/SettingsActivity.java "SettingsActivity.java"
check_file app/src/main/java/com/moe/nyanhelper/ThemeActivity.java "ThemeActivity.java"
check_file app/src/main/AndroidManifest.xml "AndroidManifest.xml"
check_file app/src/main/res/layout/activity_main.xml "activity_main.xml"
check_file app/src/main/res/layout/float_ball.xml "float_ball.xml"
check_file app/src/main/res/layout/float_panel.xml "float_panel.xml"
check_file app/src/main/res/layout/panel_features.xml "panel_features.xml"
check_file app/src/main/res/layout/panel_settings.xml "panel_settings.xml"
check_file app/src/main/res/layout/panel_theme.xml "panel_theme.xml"
check_file app/src/main/res/layout/activity_features.xml "activity_features.xml"
check_file app/src/main/res/layout/activity_settings.xml "activity_settings.xml"
check_file app/src/main/res/layout/activity_theme.xml "activity_theme.xml"
check_file app/src/main/res/xml/accessibility_config.xml "accessibility_config.xml"
check_file app/src/main/res/values/themes.xml "themes.xml"
check_file app/src/main/res/values/colors.xml "colors.xml"
check_file app/src/main/res/values/strings.xml "strings.xml"
check_file app/src/main/res/drawable/ball_circle.xml "ball_circle.xml"
check_file app/src/main/res/drawable/avatar_frame.xml "avatar_frame.xml"
check_file app/src/main/res/drawable/card_bg.xml "card_bg.xml"
check_file app/src/main/res/drawable/bg_main.xml "bg_main.xml"
check_file app/src/main/res/drawable/ic_launcher.xml "ic_launcher.xml(占位)"
check_file app/src/main/res/drawable/avatar_main.xml "avatar_main.xml(占位)"
check_file app/src/main/res/drawable/icon_ball.xml "icon_ball.xml(占位)"
check_file .github/workflows/build.yml "CI workflow"
check_file app/build.gradle "app/build.gradle"
check_file build.gradle "根 build.gradle"
check_file settings.gradle "settings.gradle"
check_file gradle.properties "gradle.properties"
check_file gen_icons.py "gen_icons.py"
check_file README.md "README.md"

echo ""
echo "=== 关键配置（grep 内容检查）==="
check_content 'versionName\s+"2\.0"' app/build.gradle "版本 2.0"
check_content 'compileSdk\s+34' app/build.gradle "compileSdk 34"
check_content '@drawable/ic_launcher' app/src/main/AndroidManifest.xml "①图标=第1张图"
check_content 'android:name="\.AppContext"' app/src/main/AndroidManifest.xml "Application=AppContext"
check_content '@drawable/avatar_main' app/src/main/res/layout/activity_main.xml "③主界面头像=第5张图"
check_content '@drawable/icon_ball' app/src/main/res/layout/float_ball.xml "②悬浮球=第2张图"
check_content 'centerCrop' app/src/main/res/layout/float_ball.xml "悬浮球完美覆盖(centerCrop)"
check_content '功能' app/src/main/res/layout/float_panel.xml "面板-功能页"
check_content '设置' app/src/main/res/layout/float_panel.xml "面板-设置页"
check_content '主题' app/src/main/res/layout/float_panel.xml "面板-主题页"
check_content 'swAddNya' app/src/main/res/layout/panel_features.xml "功能: 结尾加喵"
check_content 'swReplaceYou' app/src/main/res/layout/panel_features.xml "功能: 你→主人"
check_content 'swReplaceMe' app/src/main/res/layout/panel_features.xml "功能: 我→本喵"
check_content 'swSnow' app/src/main/res/layout/panel_settings.xml "设置: 雪花"
check_content 'swMeteor' app/src/main/res/layout/panel_settings.xml "设置: 流星"
check_content 'public static void setSnow' app/src/main/java/com/moe/nyanhelper/NyanConfig.java "雪花/流星互斥"
check_content 'themePink' app/src/main/res/layout/panel_theme.xml "主题-樱花粉"
check_content 'themeGreen' app/src/main/res/layout/panel_theme.xml "主题-薄荷绿"
check_content 'themePurple' app/src/main/res/layout/panel_theme.xml "主题-星空紫"
check_content 'ACTION_SET_TEXT' app/src/main/java/com/moe/nyanhelper/NyanAccessibilityService.java "无障碍全局文字替换"
check_content 'replace\("你"' app/src/main/java/com/moe/nyanhelper/NyanConfig.java "你→主人"
check_content 'replace\("我"' app/src/main/java/com/moe/nyanhelper/NyanConfig.java "我→本喵"
check_content 'appendNya' app/src/main/java/com/moe/nyanhelper/NyanConfig.java "结尾加喵"
check_content 'startSnow' app/src/main/java/com/moe/nyanhelper/EffectOverlay.java "雪花动画"
check_content 'startMeteor' app/src/main/java/com/moe/nyanhelper/EffectOverlay.java "流星动画"
check_content 'gradle-version:\s*8\.4' .github/workflows/build.yml "CI Gradle 8.4"
check_content 'gradle:8\.3\.2' build.gradle "AGP 8.3.2"
check_content 'dependencyResolutionManagement' settings.gradle "settings 管仓库"
check_content 'FeaturesActivity' app/src/main/java/com/moe/nyanhelper/MainActivity.java "主界面→功能"
check_content 'SettingsActivity' app/src/main/java/com/moe/nyanhelper/MainActivity.java "主界面→设置"
check_content 'ThemeActivity' app/src/main/java/com/moe/nyanhelper/MainActivity.java "主界面→主题"
check_content 'EffectOverlay' app/src/main/java/com/moe/nyanhelper/FloatService.java "悬浮球绑定特效"
check_content 'NyanAccessibilityService' app/src/main/AndroidManifest.xml "无障碍服务注册"
check_content 'FloatService' app/src/main/AndroidManifest.xml "悬浮服务注册"
check_content 'setClipToOutline|clipToOutline|OutlineProvider' app/src/main/java/com/moe/nyanhelper/FloatService.java "圆形裁剪代码"

echo ""
echo "=== 总计: $OK 通过, $ERR 错误 ==="
[ $ERR -eq 0 ]
