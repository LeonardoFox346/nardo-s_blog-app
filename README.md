# 博客专属APP备份（基于安卓WebView）

个人博客「[纳多狐狐实验室](https://leonardofox346.github.io)」的专属 Android 客户端 —— **一个用 WebView 把博客装进手机的小应用**。

本仓库是这份 APP 的**源码备份**，防止只存本机导致丢失。
可安装的签名安装包见右侧 **[Releases](../../releases)**（不进 git 仓库）。

| 项目 | 值 |
| --- | --- |
| 应用名 | Lab.Nardo |
| 包名 | `com.leonardofox346.io` |
| 版本 | `2.5.7_final`（versionCode 5） |
| 形态 | 单 Activity 多标签 WebView 浏览器，全部代码为 Java |
| 最低系统 | Android 7.0（API 24） |
| 目标系统 | Android 14（API 34） |
| 主页 | <https://leonardofox346.github.io> |
| 音乐页 | <https://leonardofox346.github.io/music/> |
| 新标签页 | <https://www.bing.com> |

---

## 一、它是什么

不是通用浏览器，而是**只服务于这个博客的壳**：把站点页面装进原生外壳，顺带补上手机浏览器里不够顺手的地方（多标签、返回手势、清理缓存、隐私协议与权限说明）。

## 二、功能

| 功能 | 说明 |
| --- | --- |
| 启动页 | 开屏页 `SplashActivity` |
| 隐私同意 | 首次启动须阅读并同意隐私协议（`PrivacyConsentActivity`） |
| 多标签浏览 | 最多同时活跃 **1–9** 个标签，数量可在设置里用滑块调（默认 3）；标签集合页可查看/切换/关闭（`TabManagerActivity`） |
| 导航栏 | 后退 / 前进 / 刷新 / 主页 / 新标签 / 更多 |
| 「更多」菜单 | 返回主页、**来点音乐？**（直接打开音乐的页面）、标签页集合、设置 |
| 下载 | 页面内下载交给系统下载器 |
| 清除缓存 | 设置里一键清 WebView 缓存与 `webview.db` |
| 权限说明 | 设置里以对话框列出申请了哪些权限、为什么 |
| 隐私协议 | 内置页面，另有「关于此网站」「关于APP作者」「关于 KIMI Code」入口 |
| 外部唤起 | 支持其他应用以 `open_url` 参数直接打开指定网址 |

> 申请的系统权限只有两个：`INTERNET`、`ACCESS_NETWORK_STATE`（见 `AndroidManifest.xml`）。
> 设置页的权限说明里还提到可选的 `WRITE_EXTERNAL_STORAGE`，但**清单里并没有声明**，属于文案遗留，以清单为准。

## 三、源码结构

```
2.0/
├── app/
│   ├── build.gradle                    应用模块配置（版本号在这里改）
│   ├── proguard-rules.pro              混淆规则（release 未开启混淆）
│   └── src/main/
│       ├── AndroidManifest.xml         权限、Activity 注册
│       ├── java/com/leonardofox346/io/ 全部 Java 源码（10 个类）
│       └── res/                        布局、图标、颜色、字符串
├── gradle/wrapper/                     Gradle Wrapper（8.14.5）
├── build.gradle                        顶层构建脚本（AGP 8.13.2）
├── settings.gradle                     工程名 Lab.Nardo
└── 1.png                               工程截图
```

主要类：

| 类 | 作用 |
| --- | --- |
| `SplashActivity` | 启动页 |
| `PrivacyConsentActivity` / `PrivacyPolicyActivity` | 隐私同意 / 隐私协议内容 |
| `MainActivity` | 主界面：标签管理、WebView 配置、导航与菜单 |
| `Tab` / `TabAdapter` / `TabCardAdapter` | 标签数据模型与两种列表适配器 |
| `TabManagerActivity` | 标签页集合页 |
| `SettingsActivity` | 设置：清缓存、活跃标签数、权限声明、隐私、关于 |
| `AboutKimiActivity` | 关于 KIMI Code |

## 四、怎么构建

前置：**Android Studio**（或本地 Android SDK）+ **JDK 17**。

```bash
# 1) 让 Gradle 知道 SDK 在哪（此文件不入库，需自己生成）
#    在工程根目录新建 local.properties，内容：
#    sdk.dir=C\:\\Users\\<你的用户名>\\AppData\\Local\\Android\\Sdk

# 2) 构建 debug 包
./gradlew.bat assembleDebug        # Windows
./gradlew assembleDebug            # macOS / Linux

# 3) 构建 release 包（未配置签名则输出未签名包）
./gradlew.bat assembleRelease
```

产物位置：`app/build/outputs/apk/debug/app-debug.apk`。
打正式安装包时需自行用 Android Studio 配置签名（`Build → Generate Signed Bundle / APK`）。

## 五、怎么安装

1. 打开本仓库的 **[Releases](../../releases)** 页面；
2. 在最新版本的 Assets 里下载 `app-release.apk`；
3. 手机上传安装（首次需允许「安装未知来源应用」）。

该 APK 为 release 包，已用 v2 签名方案签名（证书 CN=LeonardoFox）。
更换签名密钥会导致无法覆盖安装，升级需先卸载旧版。

## 六、备注

- 本仓库是**私有备份仓库**，仅存放 APP 源码；文章、站点配置等博客内容在另一个仓库；
- `local.properties`、`build/`、`.gradle/`、`.idea/` 中的本机文件、APK 产物均**不入库**（见 `.gitignore`）；
- 工程原本只存在于本机、没有任何备份，本仓库是它的第一份异地备份。

---

**© LeonardoFox346** · 仅用于个人博客，未做通用化，按需自取。
