#!/usr/bin/env bash
# 打包 NyanHelper-v3 为 zip（不含 gradle-wrapper.jar，按方案 A 由 CI 处理）
set -e
cd "$(dirname "$0")"

OUT="NyanHelper-3.0.zip"
rm -f "$OUT"

# 要打包的文件清单（全部是文本源码，无二进制）
FILES=(
  "README.md"
  "build.gradle"
  "settings.gradle"
  "gradle.properties"
  ".github/workflows/build.yml"
  "app/build.gradle"
  "app/src/main/AndroidManifest.xml"
  "app/src/main/java/com/moe/nyanhelper/MainActivity.java"
  "app/src/main/java/com/moe/nyanhelper/FloatWindowService.java"
  "app/src/main/java/com/moe/nyanhelper/NyanAccessibilityService.java"
  "app/src/main/java/com/moe/nyanhelper/NyanConfig.java"
  "app/src/main/java/com/moe/nyanhelper/ViewEffectOverlay.java"
  "app/src/main/res/layout/activity_main.xml"
  "app/src/main/res/layout/float_window.xml"
  "app/src/main/res/drawable/ic_launcher.xml"
  "app/src/main/res/drawable/avatar_main.xml"
  "app/src/main/res/drawable/icon_ball.xml"
  "app/src/main/res/drawable/bg_ball_circle.xml"
  "app/src/main/res/drawable/bg_card_white.xml"
  "app/src/main/res/drawable/bg_panel.xml"
  "app/src/main/res/drawable/bg_tab_bar.xml"
  "app/src/main/res/drawable/bg_avatar_circle.xml"
  "app/src/main/res/drawable/bg_theme_pink.xml"
  "app/src/main/res/drawable/bg_theme_mint.xml"
  "app/src/main/res/drawable/bg_theme_purple.xml"
  "app/src/main/res/values/colors.xml"
  "app/src/main/res/values/strings.xml"
  "app/src/main/res/values/styles.xml"
  "app/src/main/res/xml/accessibility_service_config.xml"
  "validate.py"
  "check_syntax.py"
  "verify_all.sh"
)

# 用 python 打包（兼容系统 python，不依赖 zip 命令）
python3 - "$OUT" "${FILES[@]}" <<'PY'
import sys, zipfile, os
out = sys.argv[1]
files = sys.argv[2:]
base = os.path.dirname(os.path.dirname(os.path.abspath(out)))
with zipfile.ZipFile(out, 'w', zipfile.ZIP_DEFLATED) as z:
    for f in files:
        p = os.path.join(base, f)
        if os.path.exists(p):
            z.write(p, f)
            print(f"  + {f}")
        else:
            print(f"  ! 缺失: {f}")
print(f"\n✅ 打包完成: {out} ({os.path.getsize(out)//1024} KB)")
PY

echo ""
echo "📦 上传 $OUT 到 GitHub，或解压后逐文件复制粘贴"
echo ""
echo "⚠️ 注意："
echo "   gradlew / gradlew.bat / gradle-wrapper.jar 未包含"
echo "   → 用方案 A：从任意 Android Studio 项目拷贝这 3 个文件到仓库"
echo "   → 或直接在 GitHub Codespaces / 本地用 Android Studio 打开编译"
