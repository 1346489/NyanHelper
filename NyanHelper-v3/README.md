# 本喵助手 v2.0

让文字变得可爱喵~ 的 Android 悬浮窗应用。

## 功能
- 🐱 全局文字替换（通过无障碍服务，不限聊天软件）
  - 结尾自动加"喵"
  - "我" → "本喵"
  - "你" → "主人"
- 🔮 悬浮球（圆形，图片完美覆盖）
  - 三页面板：**功能** / **设置** / **主题**
- ❄️ 设置页：雪花 / 流星动态特效（互斥，只能开一个）
- 🎨 主题页：樱花粉 / 薄荷绿 / 星空紫（三套背景色）

## 图标说明（需手动上传 PNG）
| 文件 | 说明 |
|------|------|
| `app/src/main/res/drawable/ic_launcher.png` | 第1张图：应用图标 |
| `app/src/main/res/drawable/avatar_main.png` | 第5张图：主界面头像 |
| `app/src/main/res/drawable/icon_ball.png` | 第2张图：悬浮球图标（圆形，会 centerCrop 铺满） |

> 上传后把下面占位文件删掉即可。

## 占位 drawable（未上传图标时使用，可删）
- `app/src/main/res/drawable/ic_launcher.xml`
- `app/src/main/res/drawable/avatar_main.xml`
- `app/src/main/res/drawable/icon_ball.xml`

## CI
Push 到 main 后，GitHub Actions 自动构建，产物：`NyanHelper-2.0-debug.apk`

## 部署注意事项
1. 仓库 Settings → Actions → General → "Read and write permissions"（允许 Actions 上传 Artifact）
2. 如需 GitHub Pages：Settings → Pages → Source = None（Android 项目不需要）
3. gradle-wrapper.jar 由 `gradle/gradle-build-action` 自动下载，无需提交
