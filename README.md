# 本喵助手 2.0

Android 悬浮窗助手：萌系 UI + 全局文本替换（无障碍）+ 动态雪花/流星特效 + 三主题切换。

## 功能
- 悬浮球（120×120，不遮挡屏幕），点击展开面板
- 面板 3 选项：**功能 / 设置 / 主题**（点击跳转对应页面）
- 功能页：句尾加喵 / 你→主人 / 我→本喵（走无障碍，不限聊天软件）
- 设置页：**只有雪花特效 + 流星特效**（互斥，动态绘制）
- 主题页：樱花粉 / 薄荷绿 / 星空紫（三背景，存 SharedPreferences）

## 工程结构
```
app/src/main/
├── java/com/moe/nyanhelper/
│   ├── NyanConfig.java          配置中心（所有开关 + 文本替换 apply）
│   ├── ThemeManager.java        主题切换
│   ├── SnowMeteorView.java      雪花/流星特效（SurfaceView 子线程绘制）
│   ├── FloatWindowService.java  悬浮窗服务（前台服务 + WindowManager）
│   ├── MainActivity.java        主界面
│   ├── FeaturesActivity.java    功能页（3 开关）
│   ├── SettingsActivity.java    设置页（雪花/流星）
│   ├── ThemeActivity.java       主题页
│   └── NyanAccessibilityService.java  无障碍文本替换
└── res/
    ├── layout/  float_window + 4 个 activity
    ├── drawable/ 背景/形状资源
    ├── values/  strings/themes（AppTheme 只在此定义，避免重复）
    └── xml/     accessibility_service_config
```

## 部署
1. 上传 4 张图到 `app/src/main/res/drawable/`：
   - `ic_launcher.png`（应用图标）
   - `avatar_main.png`（主界面头像）
   - `icon_ball.png`（悬浮球）
   - 三主题背景可选覆盖 `bg_theme_sakura/mint/starry`（已有 xml 渐变兜底）
2. Commit & push → GitHub Actions 自动构建 → 下载 APK
3. 安装后：主界面点「启动悬浮窗/特效」→ 授权悬浮窗权限 → 无障碍设置里开启「本喵助手」

## 校验
本地运行（不依赖 Android SDK）：
```bash
python3 verify.py
```
