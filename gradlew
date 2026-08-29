#!/bin/sh
# 优先使用环境变量 GRADLE_HOME，否则走系统 gradle，再否则提示安装
if [ -n "$GRADLE_HOME" ]; then
  exec "$GRADLE_HOME/bin/gradle" "$@"
elif command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle not found. Set GRADLE_HOME or run: sdk install gradle 8.7"
  exit 1
fi
