#!/bin/sh
# 本脚本替代标准 gradlew：直接调用系统 $GRADLE_HOME/bin/gradle，
# 避免加载 gradle-wrapper.jar / gradle-instrumentation-agent 引发的问题。
# CI（gradle-build-action）不使用本脚本，本脚本仅供本地 Linux/Mac 使用。
#
# 用法：./gradlew assembleDebug

set -e

# 1) 优先使用 GRADLE_HOME
if [ -n "$GRADLE_HOME" ] && [ -x "$GRADLE_HOME/bin/gradle" ]; then
  GRADLE_CMD="$GRADLE_HOME/bin/gradle"
# 2) 其次尝试 PATH 里的 gradle
elif command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
# 3) 都没有则提示安装
else
  echo "ERROR: 未找到 gradle。请安装 Gradle 7.6.4，或设置 GRADLE_HOME 环境变量。"
  echo "  macOS:  brew install gradle@7"
  echo "  Linux:  sdk install gradle 7.6.4"
  exit 127
fi

echo "BenmaoAssistant: using $GRADLE_CMD ($($GRADLE_CMD --version | grep Gradle | head -1))"
exec "$GRADLE_CMD" "$@"
