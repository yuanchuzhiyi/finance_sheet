# Android 原生端（FamilyFinance）

本目录是「家庭财务报表」Android 原生应用工程（Kotlin + Jetpack Compose），用于按年/月查看收支利润表、按日记录资产负债快照，并支持导出/分享 PDF。

## 功能概览

- 视图模式：按年 / 按月（利润表：收入、支出、结余），按日（资产负债表：资产、负债、净资产）
- 周期管理：新增年份 / 月份 / 日期并切换查看
- 科目管理：新增/删除/重命名科目，支持子科目分层汇总
- 其他信息：附注区（不参与汇总）
- 数据持久化：DataStore 保存为 JSON（kotlinx.serialization）
- PDF：预览、导出到应用 Documents 目录、系统分享

## 技术栈

- Kotlin（配合 JDK 17）
- Jetpack Compose + Material 3
- MVVM：ViewModel + StateFlow
- DataStore Preferences
- Navigation Compose（已引入，当前为单页）
- PDF：`android.graphics.pdf.PdfDocument` + `FileProvider`

## 工程结构

- `app/`：主模块
  - `app/src/main/java/com/familyfinance/sheet/MainActivity.kt`：入口 Activity
  - `app/src/main/java/com/familyfinance/sheet/ui/screens/MainScreen.kt`：主界面
  - `app/src/main/java/com/familyfinance/sheet/viewmodel/MainViewModel.kt`：状态与业务逻辑
  - `app/src/main/java/com/familyfinance/sheet/data/`：模型、DataStore、Repository
  - `app/src/main/java/com/familyfinance/sheet/util/PdfGenerator.kt`：PDF 生成/打开/分享
- `FamilyFinance-debug.apk`：调试包产物（方便快速安装）

## 开发环境

- Android Studio（需支持 AGP 8.7.x）
- Android SDK：`compileSdk=34`，`minSdk=26`
- 首次打开若提示 SDK 路径缺失：在 `android/local.properties` 配置 `sdk.dir=...`（Android Studio 通常会自动生成）

## 运行与构建

### Android Studio

1. 用 Android Studio 打开 `android/` 目录
2. Gradle Sync 完成后，选择 `app` 运行到模拟器/真机

### 命令行（可选）

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

### Release（未配置签名）

```bash
cd android
./gradlew :app:assembleRelease
```

如需上架/分发，请在 `app/build.gradle.kts` 配置签名、版本号策略，并按需开启混淆（Proguard/R8）。

## 数据与文件

- 数据：保存在 DataStore `family_finance_datastore` 中，Key 为 `report_data`（JSON）
- PDF：导出到应用专属目录 `getExternalFilesDir(DIRECTORY_DOCUMENTS)`，不需要额外存储权限
- 分享/打开：通过 `FileProvider`（见 `app/src/main/AndroidManifest.xml` 与 `app/src/main/res/xml/file_paths.xml`）

## 备注

- 当前 PDF 生成按 A4 单页绘制，内容过多时会截断；如需多页可扩展 `PdfGenerator`

## 常见问题

- Gradle 提示找不到 Java：请确保使用 JDK 17（Android Studio 可选用内置 JDK）
- 预览/分享 PDF 无响应：请确认系统存在可处理 PDF 的应用；或使用“分享 PDF”发送到微信/邮箱等
- 找不到导出的 PDF：文件位于应用专属 Documents 目录，可在应用内“预览 PDF/分享 PDF”访问
