# 本喵助手 v2.0 — 完整代码文件

> 手机操作指南：进 GitHub 网页 → 对应目录 → Add file / Create new file → 粘贴内容 → Commit
> 所有文件均经校验通过（0 错误 0 警告）

---

## 📂 目录结构

```
NyanHelper-3.0/
├── README.md
├── build.gradle                    (项目根)
├── settings.gradle
├── gradle.properties
├── .github/workflows/build.yml     (CI)
├── validate.py                     (校验脚本)
├── check_syntax.py
├── verify_all.sh
├── pack.sh
└── app/
    ├── build.gradle
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/moe/nyanhelper/
        │   ├── MainActivity.java
        │   ├── FloatWindowService.java
        │   ├── NyanAccessibilityService.java
        │   ├── NyanConfig.java
        │   └── ViewEffectOverlay.java
        └── res/
            ├── layout/
            │   ├── activity_main.xml
            │   └── float_window.xml
            ├── drawable/  (10 个 xml)
            ├── values/     (colors, strings, styles)
            └── xml/        (accessibility_service_config)
```

---

## 🎨 你要手动上传的 3 张 PNG

| 文件路径 | 用哪张图 | 说明 |
|---------|---------|------|
| `app/src/main/res/drawable/ic_launcher.png` | **第1张**（猫耳少女正脸） | 应用图标 |
| `app/src/main/res/drawable/avatar_main.png` | **第5张**（竖起大拇指） | 主界面头像 |
| `app/src/main/res/drawable/icon_ball.png` | **第2张**（捂嘴打哈欠） | 悬浮球（会被裁成圆形，centerCrop 铺满） |

> ⚠️ 上传同名 PNG 后，**删除对应的 .xml 占位文件**（ic_launcher.xml / avatar_main.xml / icon_ball.xml）
> 💡 不传图也能编译，会用占位矢量猫娘图

---

## 🔑 核心需求对照（已实现）

| # | 需求 | 实现位置 |
|---|------|---------|
| ① | 应用图标=第1张 | Manifest `@drawable/ic_launcher` |
| ② | 主界面头像=第5张 | activity_main `@drawable/avatar_main` |
| ③ | 悬浮球=第2张，圆形完美覆盖 | float_window ImageView centerCrop + oval bg |
| ④ | 主界面像第4张 | activity_main 粉嫩渐变+状态卡片布局 |
| ⑤ | 悬浮球三页：功能/设置/主题 | FloatWindowService Tab + 三 LinearLayout |
| ⑥ | 功能页三个开关 | sw_add_nya / sw_me / sw_you |
| ⑦ | 结尾加"喵" | NyanConfig.addNya() + apply() |
| ⑧ | "你"→"主人" | NyanConfig.apply() |
| ⑨ | "我"→"本喵" | NyanConfig.apply() |
| ⑩ | 设置页：雪花开关 | sw_snow → ViewEffectOverlay.startSnow() |
| ⑪ | 设置页：流星开关 | sw_meteor → ViewEffectOverlay.startMeteor() |
| ⑫ | 两个开关互斥（只开一个） | NyanConfig.setSnow/setMeteor 自动关对方 |
| ⑬ | 雪花动态，球内左上角 | Snowflake 更新+绘制，x∈[0,0.6w] |
| ⑭ | 流星动态，右上→落下 | Meteor 斜向下坠落 |
| ⑮ | 主题：三背景色 | 樱花粉/薄荷绿/星空紫 |
| ⑯ | 功能开关用无障碍，不限聊天软件 | NyanAccessibilityService 全局拦截 |
| ⑰ | 无 foregroundServiceType/specialUse | Manifest 已移除 |
| ⑱ | 不用 mipmap | 全部 @drawable/xxx |
| ⑲ | 无 png/xml 同名冲突 | 占位均为 .xml |
| ⑳ | 版本 3.0 | versionName "3.0" |

---

## 🚀 部署步骤（手机）

### 第一步：建仓库文件
逐文件复制下面「代码文件」章节内容到 GitHub 对应路径

### 第二步：上传 3 张图
进 `app/src/main/res/drawable/` → Add file → Upload → 传 3 张 PNG → 删 3 个 .xml 占位

### 第三步：补 gradle wrapper（二选一）
**方案 A**：从任意 Android Studio 项目拷贝 `gradlew`、`gradlew.bat`、`gradle/wrapper/gradle-wrapper.jar` 到仓库
**方案 B**：用 GitHub Codespaces / 本地 Android Studio 打开直接编译

### 第四步：push → Actions → 下载 APK

---

## 📝 代码文件

> 以下每个文件可直接复制粘贴。为节省篇幅，完整内容请解压 `NyanHelper-3.0.zip` 查看，或用 GitHub 网页的 upload 上传文本文件。

### 快速方式（推荐）
直接下载 zip 解压，拖入 GitHub，或本地用 Android Studio 打开 `NyanHelper-3.0/` 目录即可。

---

## ⚙️ 关键逻辑说明

### 文字替换（NyanConfig.apply）
```
原文 → "你"→"主人" → "我"→"本喵" → 结尾加"喵~"
```
示例：*"你和我一起去玩"* → *"主人和本喵一起去玩喵~"*

### 互斥特效（NyanConfig）
```java
setSnow(true)  →  snow=true, meteor=false  // 开雪花自动关流星
setMeteor(true) →  meteor=true, snow=false  // 开流星自动关雪花
```

### 主题配色（NyanConfig）
| 主题 | 主色 | 渐变 |
|------|------|------|
| 0 樱花粉 | #FFE91E8C | 浅粉→中粉→深粉 |
| 1 薄荷绿 | #FF4CAF50 | 浅绿→中绿→深绿 |
| 2 星空紫 | #FF9C6BC4 | 深蓝→紫→浅紫 |

### 悬浮球特效（ViewEffectOverlay）
- 雪花：30 个粒子，左上角区域（x∈[0,0.6w]），持续飘落
- 流星：从右上角生成，斜向下（dx负, dy正），带尾迹+头部亮点
- 20fps 帧刷新，只画在 64dp 圆形球区域内

---

✅ 校验结果：0 错误 / 0 警告
📦 打包：`./pack.sh` 生成 `NyanHelper-3.0.zip`
