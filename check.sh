#!/bin/bash
# 精确检查：针对你提的 4 个问题
# 1. 悬浮窗是否有 3 个选项（功能/设置/主题）
# 2. 选项里是否有开关，能否调节
# 3. 代码引用一致性（id、方法、资源）
set -e
cd "$(dirname "$0")"

JAVA=app/src/main/java/com/moe/nyanhelper
RES=app/src/main/res

echo "══════════════════════════════════════════════════════════════"
echo "  检查 1：悬浮窗面板是否包含 3 个选项（功能/设置/主题）"
echo "══════════════════════════════════════════════════════════════"
FLOAT_LAYOUT="$RES/layout/float_window.xml"
for id in tabFeatures tabSettings tabTheme; do
  if grep -q "android:id=\"@+id/$id\"" "$FLOAT_LAYOUT"; then
    echo "  ✅ float_window.xml 有 @+id/$id"
  else
    echo "  ❌ float_window.xml 缺少 @+id/$id"
  fi
done

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 2：Java 是否给这 3 个选项设置了点击跳转"
echo "══════════════════════════════════════════════════════════════"
SERVICE="$JAVA/FloatService.java"
for tab in tabFeatures tabSettings tabTheme; do
  if grep -qE "R\.id\.$tab|bindTab\(R\.id\.$tab" "$SERVICE"; then
    echo "  ✅ FloatService 绑定了 R.id.$tab"
  else
    echo "  ❌ FloatService 未绑定 R.id.$tab"
  fi
done

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 3：设置页是否只有雪花/流星两个开关"
echo "══════════════════════════════════════════════════════════════"
SETTINGS_LAYOUT="$RES/layout/activity_settings.xml"
SWITCHES=$(grep -o 'android:id="@+id/sw[A-Za-z]*"' "$SETTINGS_LAYOUT" | wc -l)
echo "  activity_settings.xml 中 Switch 数量: $SWITCHES"
if [ "$SWITCHES" -eq 2 ]; then
  echo "  ✅ 只有 2 个开关 (swSnow / swMeteor)"
else
  echo "  ⚠️  开关数 = $SWITCHES，期望 2"
fi
grep -o 'android:id="@+id/sw[A-Za-z]*"' "$SETTINGS_LAYOUT" | sed 's/^/    - /'

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 4：NyanConfig 方法完整性（被调用的都要有）"
echo "══════════════════════════════════════════════════════════════"
CONFIG="$JAVA/NyanConfig.java"
# 收集 Java 里对 NyanConfig 的所有调用
grep -rho "NyanConfig\.[a-zA-Z]*" "$JAVA" | sed 's/NyanConfig\.//' | sort -u > build_called.txt
echo "  代码中调用的 NyanConfig 成员："
cat build_called.txt | sed 's/^/    - /'

echo ""
echo "  在 NyanConfig.java 中的定义情况："
for name in THEME_SAKURA THEME_MINT THEME_STARRY \
            isAddNya setAddNya \
            isYouToMaster setYouToMaster \
            isIToMe setIToMe \
            isReplaceYou setReplaceYou \
            isReplaceMe setReplaceMe \
            isSnow setSnow \
            isMeteor setMeteor \
            isServiceRunning setServiceRunning \
            getTheme setTheme \
            apply; do
  # 方法或字段定义：用固定字符串匹配多种模式
  if grep -qF "$name(" "$CONFIG" \
     || grep -qE "public static final int $name\b" "$CONFIG" \
     || grep -qF "$name =" "$CONFIG"; then
    : # echo "    ✅ $name"
  else
    echo "    ❌ 未定义: $name"
  fi
done

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 5：资源 id 引用 ↔ layout 定义 一致性"
echo "══════════════════════════════════════════════════════════════"
# 提取所有 @+id/ 定义
find "$RES/layout" -name "*.xml" -exec grep -ho '@+id/[a-zA-Z0-9_]*' {} \; | sed 's/@+id\///' | sort -u > build_defined_ids.txt
# 提取所有 R.id. 使用
grep -rho "R\.id\.[a-zA-Z0-9_]*" "$JAVA" | sed 's/R\.id\.//' | sort -u > build_used_ids.txt

echo "  使用了但未定义的 id："
comm -23 build_used_ids.txt build_defined_ids.txt | sed 's/^/    ❌ /'
echo "  （无输出 = 全部 OK）"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 6：drawable 引用 ↔ 文件 一致性"
echo "══════════════════════════════════════════════════════════════"
# 收集 layout/xml 里 @drawable/xxx
grep -rho "@drawable/[a-zA-Z0-9_]*" "$RES" | sed 's/@drawable\///' | sort -u > build_used_drawables.txt
# 收集 res/drawable 下实际文件（去扩展名）
find "$RES/drawable" -type f | sed 's|.*/||;s|\.[^.]*$||' | sort -u > build_exist_drawables.txt
echo "  引用的 drawable："
cat build_used_drawables.txt | sed 's/^/    - /'
echo "  缺失的 drawable（引用了但文件不存在）："
comm -23 build_used_drawables.txt build_exist_drawables.txt | sed 's/^/    ❌ /'
echo "  （无输出 = 全部 OK）"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 7：strings.xml 是否包含代码/Manifest 引用的字符串"
echo "══════════════════════════════════════════════════════════════"
grep -rho "@string/[a-zA-Z0-9_]*" "$RES" "$JAVA" 2>/dev/null | sed 's/@string\///' | sort -u > build_used_strings.txt
grep -ho 'name="[a-zA-Z0-9_]*"' "$RES/values/strings.xml" | sed 's/name="//;s/"//' | sort -u > build_defined_strings.txt
echo "  引用了但 strings.xml 未定义的字符串："
comm -23 build_used_strings.txt build_defined_strings.txt | sed 's/^/    ❌ /'
echo "  （无输出 = 全部 OK）"

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 8：ThemeManager vs NyanTheme 命名统一"
echo "══════════════════════════════════════════════════════════════"
if [ -f "$JAVA/ThemeManager.java" ]; then
  echo "  ✅ 存在 ThemeManager.java"
fi
if grep -q "NyanTheme" "$JAVA"/*.java; then
  echo "  ⚠️  代码里用了 'NyanTheme'，但类名是 ThemeManager，会编译错误："
  grep -ln "NyanTheme" "$JAVA"/*.java | sed 's/^/    - /'
else
  echo "  ✅ 无 NyanTheme/ThemeManager 命名冲突"
fi

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 9：accessibility config 引用的字符串是否存在"
echo "══════════════════════════════════════════════════════════════"
ACC="$RES/xml/accessibility_service_config.xml"
if grep -q "@string/accessibility_desc" "$ACC"; then
  if grep -q 'name="accessibility_desc"' "$RES/values/strings.xml"; then
    echo "  ✅ accessibility_desc 已定义"
  else
    echo "  ❌ accessibility_desc 未定义"
  fi
fi

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 10：SnowMeteorView 是否被 FloatService 正确使用"
echo "══════════════════════════════════════════════════════════════"
if grep -q "SnowMeteorView" "$SERVICE"; then
  echo "  ✅ FloatService 引用了 SnowMeteorView"
  if grep -q "effectView.refreshConfig" "$SERVICE"; then
    echo "  ✅ 调用了 refreshConfig（开关→特效联动）"
  else
    echo "  ⚠️  未调用 refreshConfig，开关可能无法驱动特效"
  fi
else
  echo "  ❌ FloatService 未引用 SnowMeteorView"
fi

echo ""
echo "══════════════════════════════════════════════════════════════"
echo "  检查 11：功能页开关（FeaturesActivity）是否可调节"
echo "══════════════════════════════════════════════════════════════"
FEAT="$JAVA/FeaturesActivity.java"
for sw in swAddNya swYouToMaster swIToMe; do
  if grep -q "R.id.$sw" "$FEAT"; then
    echo "  ✅ 绑定 $sw"
  else
    echo "  ❌ 未绑定 $sw"
  fi
done

echo ""
echo "全部检查完成。"
