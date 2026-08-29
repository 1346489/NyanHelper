#!/bin/bash
set -e
cd /data/workspace/BenmaoAssistant

export JAVA_HOME=/opt/jdk-17
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

echo "=== [1/5] 下载 Temurin JDK 17 ==="
if [ ! -d "$JAVA_HOME" ]; then
  curl -sL -o /tmp/jdk17.tar.gz "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
  mkdir -p $JAVA_HOME
  tar -xzf /tmp/jdk17.tar.gz -C $JAVA_HOME --strip-components=1
fi
java -version 2>&1 | head -1

echo "=== [2/5] 下载 Android cmdline-tools ==="
mkdir -p $ANDROID_HOME
if [ ! -d "$ANDROID_HOME/cmdline-tools" ]; then
  curl -sL -o /tmp/cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  unzip -q /tmp/cmdtools.zip -d $ANDROID_HOME
  mkdir -p $ANDROID_HOME/cmdline-tools/latest
  mv $ANDROID_HOME/cmdline-tools/bin $ANDROID_HOME/cmdline-tools/lib $ANDROID_HOME/cmdline-tools/NOTICE.txt $ANDROID_HOME/cmdline-tools/source.properties $ANDROID_HOME/cmdline-tools/latest/ 2>/dev/null || true
fi

echo "=== [3/5] 安装 SDK 组件 (platform-34, build-tools 34) ==="
yes | sdkmanager --sdk_root=$ANDROID_HOME --licenses >/dev/null 2>&1 || true
sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null 2>&1 || true
echo "SDK packages installed:"
sdkmanager --sdk_root=$ANDROID_HOME --list_installed 2>/dev/null | grep -E "platforms|build-tools" | head

echo "=== [4/5] 构建 debug APK ==="
./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tail -40
