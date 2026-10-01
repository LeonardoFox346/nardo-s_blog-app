# Lab.Nardo

个人博客「[纳多狐狐实验室](https://nardofox.cn)」的专属 Android 客户端 —— **一个把博客装进手机的 WebView 外壳**。

界面按 **Material 3** 规范重做，全部代码为 Java，无第三方统计、无广告、不收集个人信息。

| 项目 | 值 |
| --- | --- |
| 应用名 | Lab.Nardo |
| 包名 | `com.leonardofox346.io` |
| 版本 | `3.0.1_beta`（versionCode 7） |
| 形态 | 单 Activity 多标签 WebView，全部代码为 Java（12 个类） |
| 最低系统 | Android 10（API 29） |
| 目标 / 编译 | Android 17（API 37） |
| 许可 | Apache License 2.0（**仅及于代码**，详见下方「开源许可」） |

---

## 一、它是什么

不是通用浏览器，而是**只服务于这个博客的壳**：把站点页面装进原生外壳，补上手机浏览器里不够顺手的地方 —— 多标签、加载进度、返回手势、缓存清理，以及一份真正讲清楚的隐私说明。

## 二、功能

| 功能 | 说明 |
| --- | --- |
| 启动页 | 开屏页 `SplashActivity` |
| 隐私同意 | 首次启动弹出**摘要式弹窗**（不是整页），三条摘要把话说完；「不同意」即退出应用（`PrivacyConsentActivity`） |
| 多标签浏览 | 最多同时活跃 **1–9** 个标签，数量可在设置里调（默认 3）；标签集合页可查看 / 切换 / 休眠 / 关闭（`TabManagerActivity`） |
| 加载进度 | 工具栏下方一条进度条，加载期间不再是无反馈的白屏 |
| 导航栏 | 后退 / 前进 / 刷新 / 新标签 / 更多 |
| 「更多」菜单 | 返回主页、来点音乐？、标签页集合、设置 |
| 下载 | 页面内下载会被拦截并提示，**不保存任何文件** |
| 清除缓存 | 设置里一键清 WebView 缓存与 Cookie |
| 设置页 | 清除缓存 / 活跃标签页数量 / 权限声明 / 隐私与条款 / 关于此网站 / 关于APP作者 / 关于本应用 |
| 关于本应用 | 版本信息 + 开发历程时间线 + 「AI 辅助编程」入口 + **开源许可**说明 |
| 深色模式 | 跟随系统，含状态栏与导航栏 |
| 外部唤起 | 支持其他应用以 `open_url` 参数直接打开指定网址 |

> 申请的系统权限**只有两个**：`INTERNET`、`ACCESS_NETWORK_STATE`（见 `AndroidManifest.xml`）。
> 没有定位、通讯录、相机、麦克风、存储等任何其他权限。

## 三、下载安装

1. 打开本仓库的 **[Releases](../../releases)** 页面；
2. 在最新版本的 Assets 里下载 `app-release.apk`；
3. 手机上安装（首次需允许「安装未知来源应用」）。

该 APK 已用 **v2 签名方案**签名（证书 `CN=LeonardoFox`）。

> ⚠️ **升级提示**：自 `3.0.0` 起本应用改用了**新的签名密钥**（旧密钥已于 2026-09-19 确认丢失、不可复现）。
> 已安装 **2.5.7 及更早版本**的用户，升级前需要**先卸载旧版**，本地数据（偏好与缓存）会随之丢失。

## 四、从源码构建

前置：**Android Studio**（或本地 Android SDK）+ **JDK 17 及以上**（实测 JDK 21）。
工具链：**AGP 9.2.1** + **Gradle Wrapper 9.7.1**。

```bash
# 1) 让 Gradle 知道 SDK 在哪（此文件不入库，需自己生成）
#    在工程根目录新建 local.properties，内容：
#    sdk.dir=C\:\\Users\\<你的用户名>\\AppData\\Local\\Android\\Sdk

# 2) debug 包
./gradlew.bat assembleDebug        # Windows
./gradlew assembleDebug            # macOS / Linux

# 3) release 包
./gradlew.bat assembleRelease
```

产物：`app/build/outputs/apk/debug/app-debug.apk`。

**关于 release 签名**：签名配置从工程根目录的 `keystore.properties` 读取（该文件**不入库**，且 `*.jks` / `*.keystore` 均已被 `.gitignore` 挡住）。
该文件不存在时构建脚本会**静默跳过**签名，`assembleRelease` 输出未签名包 —— 所以没有密钥的人拉下代码照样能编译运行。

## 五、源码结构

```
2.0/
├── app/
│   ├── build.gradle                    应用模块配置（版本号在这里改）
│   ├── proguard-rules.pro              混淆规则（release 未开启混淆）
│   └── src/main/
│       ├── AndroidManifest.xml         权限、Activity 注册
│       ├── java/com/leonardofox346/io/ 全部 Java 源码（12 个类）
│       └── res/                        布局、图标、颜色、字符串
├── gradle/wrapper/                     Gradle Wrapper（9.7.1）
├── build.gradle                        顶层构建脚本（AGP 9.2.1）
├── settings.gradle                     工程名 Lab.Nardo
├── LICENSE                             Apache License 2.0 全文
└── NOTICE                              版权与许可范围声明（品牌排除）
```

主要类：

| 类 | 作用 |
| --- | --- |
| `SplashActivity` | 启动页 |
| `PrivacyConsentActivity` | 首次启动的隐私摘要弹窗（同意 / 不同意，返回键等同不同意） |
| `PrivacyPolicyActivity` | 隐私与条款正文页（两部分、15 条） |
| `MainActivity` | 主界面：标签管理、WebView 配置、导航与菜单 |
| `Tab` | 标签数据模型（URL、标题、WebView、休眠状态） |
| `TabUtils` | WebView 工厂与休眠 / 唤醒 / 销毁 |
| `TabCardAdapter` | 标签卡片适配器 |
| `TabManagerActivity` | 标签页集合页 |
| `SettingsActivity` | 设置页 |
| `AboutAiActivity` | 关于本应用（开发历程 + 开源许可入口） |
| `AiAssistActivity` | AI 辅助编程（DeepSeek 与 Kimi 两段） |
| `UiInsets` | 边到边（edge-to-edge）系统栏内边距工具 |

## 六、隐私

**不收集任何个人信息。** 没有账号体系，不接入第三方统计，不投放广告，不写入任何 Cookie，也不索取位置、通讯录与存储等权限。

应用只在设备本地保存三样东西：活跃标签页数量偏好、WebView 网页缓存、一个记录「是否已同意条款」的布尔值 —— 全部在应用私有目录内，卸载即删除。

本应用通过系统 WebView 加载网页，网络请求与用浏览器直接访问并无不同。除您主动浏览的网页请求之外，本应用不向任何服务器发送数据。

完整条款见应用内 **设置 → 隐私与条款**，与网站 [nardofox.cn/privacy/](https://nardofox.cn/privacy/) 同步。

## 七、开源许可

源代码以 **[Apache License 2.0](LICENSE)** 授权。您可以自由使用、修改、再分发，但须保留版权声明与许可声明。

> **该许可仅及于代码本身。** 以下内容**不在**授权范围内，详见 [NOTICE](NOTICE)：
>
> - **品牌与形象** ——「纳多狐狐」形象（委托创作）、「Lab.Nardo」与「纳多狐狐实验室」名称；
> - **第三方品牌标识** —— DeepSeek 与 Kimi 的标识，各归其权利人所有，本仓库仅作说明性使用；
> - **网站内容** —— `nardofox.cn` 上的图文内容，其授权规则（默认「禁止转载引用」，并禁止用于 AI 模型训练）见网站《隐私政策与使用条款》。

依 Apache License 2.0 第 6 条，本许可**不授予商标权**。若您基于本仓库发布衍生版本，请更换应用名称与应用图标，勿使他人误认为该版本由纳多狐狐实验室发布。

---

**© 2026 LeonardoFox** · 个人项目，未作通用化，按需自取。
