# Android 迁移交付状态

## 已编写

- Android Gradle 工程、官方 Gradle Wrapper（8.11.1）和发行包 SHA-256 校验配置。
- Android 原生界面、单个一次性闹钟、试听、锁屏测试、停止及贪睡。
- 到点启动的媒体前台服务、音频焦点、平滑渐强、循环不重置音量、限时唤醒锁和自动停止。
- 权限状态、通知操作、全屏通知入口、重启和时间变化后的定时恢复。
- 无网络权限的本地设置与原创恒定音量循环音色。
- Windows 本地构建脚本、可选手动 GitHub Android 构建流程、计划和真机记录表。
- 原 iOS 工程已移至 legacy/ios，不参与当前构建。

## 本机环境

初始检测到 Java 8 运行时，未发现可用的 Android SDK、adb 或系统 Gradle。
当前 Android Gradle Plugin 需要 JDK 17 和 Android SDK Platform 35。
安装与启动 Android Studio 的详细步骤在根目录 README 中。

为本轮检查在项目忽略目录 `.tools/` 中准备了临时 JDK 17 和 AOSP Android 35 公共 API jar。
没有改变系统 Java 配置。临时 API jar 不等于完整 SDK，也不是手机运行环境。
官方 SDK 下载端点在当前连接下出现 SSL 连接错误，未安装完整 Android SDK。

## 验证结果

- **已通过 Java 编译检查**：全部应用 Java 源文件针对 AOSP Android 35 公共 API 编译通过。
  资源类 R 使用临时桩，仅验证源码语法和 API 类型，不代替 Android 资源编译、Manifest 合并、DEX 或 APK 打包。
- **已通过核心检查**：播放增益范围、单调渐强、达到上限后保持、跨音频循环不重置，以及跨午夜、跨年、当天未来时间的计算。
- **已通过静态资源检查**：Manifest / 样式 / 图标 XML 可解析；音频是 4 秒单声道 PCM16 / 22050 Hz，无数字削波，循环边界没有额外的大幅跳变。
- **已校验 Gradle Wrapper**：下载自 Gradle 官方仓库，wrapper JAR 的 SHA-256 与官方发布值一致；发行包下载配置也包含官方 SHA-256。
- **未完成完整 Gradle 构建**：缺少完整 Android SDK，尚未生成 APK、未运行 Android Lint、模拟器或真机测试。

源码和 API 编译检查通过不代表锁屏响铃已经通过真机验证。

## 尚未执行的外部操作

未创建或上传 GitHub 仓库，未触发云端构建，未付费，未连接安卓手机。
测试设备已确认为 Redmi Note 10、Android 11（API 30）；属于 minSdk 26 的支持范围。
MIUI 具体版本待补充，后台权限与全屏行为待真机确认。已更新该设备的安装与测试指引，尚未在设备上实际运行。
