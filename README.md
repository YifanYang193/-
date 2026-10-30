# 轻柔闹钟 · Android 自用版

项目已从 iOS 改为 **原生 Android App**。目标是手机正常锁屏休息，到点才启动声音渐强，平时无需服务常驻或保持屏幕开启。

**APK 要安装在安卓手机上，不能安装到 iPhone，也不会改变 iPhone 的操作系统。**
已确认测试设备：**Redmi Note 10，Android 11（API 30）**，MIUI 版本待补充。最低支持 Android 8.0，当前工程覆盖该设备的系统版本。

**当前状态：源代码已完成，13 个应用 Java 文件通过 Android 35 API 编译检查；尚未进行完整 SDK 构建，没有生成 APK。**
本机还需配置 Android SDK。详情见 [交付状态](docs/IMPLEMENTATION-STATUS.md)。

## 当前功能

- 一个待响的一次性闹钟，按所选时间的下一次到达时刻安排。
- 10 秒、30 秒、1 分钟、2 分钟、3 分钟渐强；达到播放上限后保持。
- 播放上限 10%～100%，不改变系统的全局闹钟音量。
- 原创柔和音色循环，渐强由播放器按实际经过时间控制，音频循环不会重置音量包络。
- 30 秒前台试听、1 分钟后锁屏测试。
- 停止响铃、贪睡 5 分钟、取消待响闹钟。
- 通知上的操作按钮；系统允许时显示锁屏响铃界面。
- 重启后恢复未来的待响闹钟；时间或时区修改后按所选当地时间重新安排。
- 只在响铃期间持有 CPU 唤醒锁、运行媒体前台服务，10 分钟后自动停止。
- 本地保存，无登录、广告、联网权限或服务器。

本版暂不含晨光、每周重复、多闹钟、音色选择。新设置或测试会替换现有待响闹钟，并先提示确认。

## 与原 iOS 方案的变化

| 项目 | Android 方案 |
| --- | --- |
| 开发电脑 | 直接使用 Windows |
| 开发工具 | 免费 Android Studio、Android SDK、Gradle |
| 安装 | 生成 APK，USB 调试安装或传到手机手动安装 |
| 账号 | 不需要 Apple 账号或付费开发者会员 |
| 有效期 | 自签名 APK 没有 7 天续签规则；保管签名密钥便于后续更新 |
| GitHub | 可选，本地编译无需公开源码 |
| 后台调度 | 系统 AlarmManager.setAlarmClock，不使用整夜轮询 |

旧代码在 `legacy/ios/` 中归档保存，不参与 Android 构建。
旧 iOS 文件中的说明和状态仅代表历史版本，不适用于本版。

## 在 Windows 本地生成 APK（推荐）

1. 从 [Android 官方网站](https://developer.android.com/studio) 安装免费 Android Studio。
2. 首次启动，按向导安装 Android SDK，并查看、接受你同意的 SDK 许可。
3. 选择 **Open**，打开本项目根目录 `E:\轻柔闹钟`，不要打开 `app` 子目录。
4. 在 SDK Manager 中安装 **Android SDK Platform 35** 和 **Build-Tools 35.0.0**。
5. 在 Gradle 设置中选用 Android Studio 自带 JDK（17 或兼容的较新版本），不要使用本机旧 Java 8。
6. 等待 Gradle Sync 下载依赖。项目附带官方 Gradle Wrapper，会下载 Gradle 8.11.1 并校验发行包 SHA-256。
7. 使用 Build 菜单生成 APK，或打开项目终端执行：

```powershell
.\gradlew.bat assembleDebug
```

终端需已设置有效的 Java 17+、Android SDK 路径。如果图形界面能构建而终端不能，先检查 `JAVA_HOME`。
SDK 路径可以由 Android Studio 自动写入本机 `local.properties`；不要把这个文件上传到仓库。
推荐 Gradle 使用 JDK 17 或 21；不要直接使用不兼容的新 JDK。工程已为当前中文目录开启路径检查覆盖，
若某个本机 SDK 工具仍不支持中文路径，可将项目复制到 `E:\GentleAlarm` 再打开。

构建成功后的 APK：

```text
E:\轻柔闹钟\app\build\outputs\apk\debug\app-debug.apk
```

也可以运行辅助脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-android.ps1
```

调试 APK 自动使用本机调试密钥签名。**保留 `%USERPROFILE%\.android\debug.keystore`，不要分享或提交。**
换电脑、重装或换云端构建密钥后，旧安装包可能无法直接被新包覆盖。
Debug 包适合当前自用验证；长期使用可再配置自行保管的 release 密钥，不需要购买证书。

## 安装到安卓手机

### 方法一：传 APK 手动安装

将 APK 通过 USB、文件传输或你信任的方式放到安卓手机，点开安装。
若系统要求，给本次用于打开 APK 的文件管理器开启“允许安装未知应用”。
具体菜单和附加验证取决于厂商；不需要越狱或解锁 Bootloader。

### 方法二：USB 调试安装

1. 在手机“关于手机”中查找系统版本；通常连续点击版本号可开启开发者选项。
2. 开启 USB 调试，用数据线连接电脑，并在手机确认这台电脑的调试授权。
3. 在 Android Studio 选择设备并运行，或使用 SDK 的 adb：

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 首次使用和真机验证

### Redmi Note 10 / Android 11

- 无需 Android 12+ 的“闹钟和提醒”特殊授权，也没有 Android 13+ 的通知运行时授权弹窗；仍需确认应用通知没有被手动关闭。
- 无需 Android 14+ 的全屏提醒特殊授权页面；锁屏显示仍可能受 MIUI 的通知与其他权限控制。
- 在系统的应用管理中查看轻柔闹钟的“自启动”“省电策略 / 电池限制”和锁屏通知。为测试可靠性，可允许自启动、将本应用后台策略设为“无限制”，并开启锁屏通知。菜单名称以本机 MIUI 为准，不必关闭全机省电保护。
- 如果有“后台弹出界面 / 锁屏显示”选项，先检查其状态；即使全屏界面没有自动出现，也要分别检查声音和通知操作是否正常。
- 先测 1 分钟锁屏，再测至少 30 分钟锁屏，最后测试过夜唤醒；这些设置不代表应用要整夜保持运行。

小米官方支持文档也将自启动和应用后台“无限制”列为后台连接排查项；上述项目是对本闹钟的测试建议，不是该机型已通过验证的结论。

### 通用步骤

1. 打开轻柔闹钟，按页面提示允许“闹钟和提醒”和通知。
2. Android 14+ 如希望自动出现锁屏界面，进入“锁屏全屏提醒设置”允许；未允许时仍可从通知操作。
3. 在系统声音设置确认“闹钟音量”非零。勿扰模式是否允许闹钟，由你的系统设置决定；应用不擅自绕过勿扰。
4. 先选 10 秒渐强、中等播放上限，点击“1 分钟后锁屏测试”，锁屏等待。
5. 检查响铃时刻、声音是否渐强、达到上限后是否保持、停止和贪睡是否正常。
6. 再验证长时间锁屏、省电模式、重启、耳机连接等场景，记录到 `docs/DEVICE-CHECK.md`。

关机无法响铃；系统“强制停止”后需要重新打开应用。厂商的自启动、后台和省电管理也可能限制闹钟。
如果手机有这些设置，需要根据具体品牌进行适配，不能仅凭源码保证每种 ROM 都可靠。
响铃前的应用进程不要求一直存在，但不能把“从最近任务划掉”和“设置中强制停止”混为一谈。

## 可选：GitHub 免费构建

无需现在上传；本地构建是首选。如果后续使用 GitHub：

1. 自愿创建公开仓库并上传项目（不含密钥、本机配置或 `.tools`）。
2. Actions → **Build Android debug APK** → **Run workflow**。
3. 成功后下载 `GentleAlarm-Android-debug`，解压取得 APK。

工作流使用标准 Ubuntu runner，仅手动触发。公开仓库的标准 runner 目前免费。
云端构建的临时调试密钥不保证每次相同；本地包和云端包也不保证互相覆盖。
要稳定覆盖更新，优先一直在本机构建，或以后单独配置受保护的固定签名。

## 文件结构

| 路径 | 用途 |
| --- | --- |
| `app/src/main/java/com/gentlealarm/app/` | Java 原生界面、调度、渐强播放和响铃服务 |
| `app/src/main/AndroidManifest.xml` | 权限、接收器、前台服务和锁屏界面声明 |
| `app/src/main/res/raw/gentle_loop.wav` | 4 秒原创恒定音量循环音频 |
| `scripts/generate_sounds.py` | 可重复生成音色，运行不需要第三方 Python 库 |
| `scripts/build-android.ps1` | Windows 构建及可选 USB 安装 |
| `checks/CoreChecks.java` | 音量曲线与时间计算的不变量检查 |
| `scripts/check-core.ps1` | 仅需 JDK 的核心检查入口，不依赖 Android SDK |
| `docs/DEVELOPMENT-PLAN.md` | 修改后的开发计划 |
| `docs/IMPLEMENTATION-STATUS.md` | 本轮实际验证范围及限制 |
| `legacy/ios/` | 原 iOS 版本归档，不参与构建 |

## 官方依据

- [Android 精确定时与 setAlarmClock](https://developer.android.com/develop/background-work/services/alarms)
- [Android 14 的精确闹钟权限](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms)
- [前台服务后台启动的精确闹钟例外](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start)
- [Android 14 全屏通知规则](https://developer.android.com/about/versions/14/behavior-changes-14)
- [Android Gradle Plugin 8.9 与 Gradle / JDK 兼容要求](https://developer.android.com/build/releases/agp-8-9-0-release-notes)
- [小米官方后台策略排查说明](https://www.mi.com/global/support/faq/details/KA-515628/)
