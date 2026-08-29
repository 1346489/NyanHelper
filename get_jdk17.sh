#!/bin/bash
# 探测多个镜像源，下载 JDK 17
set -e
TARGET=/opt/jdk-17

try_url() {
  local url="$1"
  echo ">>> TRY: $url"
  local code=$(curl -sL -o /tmp/jdk.tar.gz "$url" -w "%{http_code}" 2>&1)
  echo "    HTTP:$code  size:$(stat -c%s /tmp/jdk.tar.gz 2>/dev/null)"
  if [ "$code" = "200" ]; then
    local ft=$(file -b /tmp/jdk.tar.gz | cut -d: -f1)
    if echo "$ft" | grep -qi "gzip\|tar\|archive"; then
      echo "    OK archive detected: $ft"
      return 0
    fi
  fi
  return 1
}

# 候选地址
URLS=(
  "https://mirrors.tencent.com/adoptium/17/jdk/x64/linux/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
  "https://mirrors.aliyun.com/adoptium/17/jdk/x64/linux/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
  "https://mirrors.huaweicloud.com/java/jdk/17.0.12+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
  "https://repo.huaweicloud.com/java/jdk/17.0.12+7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
  "https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/x64/linux/OpenJDK17U-jdk_x64_linux_hotspot_17.0.12_7.tar.gz"
)

for u in "${URLS[@]}"; do
  if try_url "$u"; then
    mkdir -p $TARGET
    tar -xzf /tmp/jdk.tar.gz -C $TARGET --strip-components=1
    echo "=== JDK17 installed to $TARGET ==="
    $TARGET/bin/java -version 2>&1 | head -2
    exit 0
  fi
done

echo "ALL FAILED. Listing adoptium dir via tencent:"
curl -sL "https://mirrors.cloud.tencent.com/adoptium/" 2>&1 | head -30
exit 1
