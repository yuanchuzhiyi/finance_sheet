# iOS 端（FamilyFinance）

本目录是对 `android/` 下 **FamilyFinance**（Kotlin + Jetpack Compose）工程的 iOS（Swift + SwiftUI）等价重写版本源码。

> 说明：当前仓库环境未包含 Xcode 工程文件（`.xcodeproj`），你可以在 Xcode 中新建一个 SwiftUI App 工程，然后将 `ios/FamilyFinance/` 下源码整体拷贝进工程（或作为源码目录引用）即可运行。

## 目录映射（Android → iOS）

Android（当前工程实际结构）：

- `android/app/src/main/java/.../ui/`：Compose UI / Navigation / 组件
- `android/app/src/main/java/.../viewmodel/`：ViewModel（StateFlow）
- `android/app/src/main/java/.../data/`：Model / DataStore / Repository
- `android/app/src/main/java/.../util/`：PDF 生成与分享

iOS（本目录）：

- `ios/FamilyFinance/Views/`：SwiftUI 页面与组件（等价于 Android `ui/`）
- `ios/FamilyFinance/ViewModels/`：`ObservableObject`（等价于 Android `viewmodel/`）
- `ios/FamilyFinance/Models/`：`Codable + Identifiable` 数据模型（等价于 Android `data/model/`）
- `ios/FamilyFinance/Services/`：数据持久化、PDF 等服务（等价于 Android `data/local/` + `data/repository/` + `util/`）
- `ios/FamilyFinance/Utilities/`：通用工具（日期、颜色、ShareSheet 等）

## 数据与异步（Flow/DataStore → async/await + JSON 文件）

Android 端使用 `DataStore Preferences` 持久化 JSON（`kotlinx.serialization`）。iOS 端为了保持：

- 数据结构一致（仍是 JSON）
- 依赖最少（不引入第三方 DB）
- 数据规模较小（无需复杂查询）

采用 **`Codable` + 本地 JSON 文件** 存储到 App Support 目录（见 `ios/FamilyFinance/Services/`）。

异步从 `StateFlow`/`Flow` 映射到 iOS：

- UI 绑定：`@Published` + SwiftUI 自动刷新
- 持久化：`async/await`（ViewModel 内部用 `Task` 调用 Repository）

## 架构映射（Android MVVM → iOS MVVM）

- **View（UI）**
  - Android：Jetpack Compose（`ui/screens/*` + `ui/components/*`）
  - iOS：SwiftUI（`ios/FamilyFinance/Views/*` + `Views/Components/*`）
- **ViewModel（状态/交互）**
  - Android：`AndroidViewModel` + `StateFlow`
  - iOS：`@MainActor ObservableObject` + `@Published`（`ios/FamilyFinance/ViewModels/*`）
- **Data / Repository（数据访问）**
  - Android：`DataStoreManager` + `ReportRepository` / `BillRepository`
  - iOS：`JSONFileStore` + `ReportRepository` / `BillRepository`（`actor`，见 `ios/FamilyFinance/Services/*`）

> 本项目 Android 端没有单独的 UseCase 层（Repository 很薄），iOS 端也保持一致：直接由 ViewModel 调用 Repository。

## 核心模型设计（Kotlin data class → Swift struct）

对应关系示例：

- `ReportData` / `CategoryGroup` / `ReportItem` / `BillBookData` / `Bill` → Swift `struct`

设计要点：

- **不可变性/可预测性**：使用 `struct`（值语义），更新通过返回“新副本”实现（例如 `ReportData.updatingGroup(...)`）
- **SwiftUI 绑定友好**：ViewModel 持有 `@Published var reportData`，每次赋新值触发界面刷新
- **持久化**：全部模型实现 `Codable`，对应 Android 的 JSON 序列化/反序列化
- **列表渲染**：实体实现 `Identifiable`（`id: String` 与 Android 保持一致）

## 页面与导航（Compose/NavController → SwiftUI/NavigationStack）

- Android `MainNavigation`（底部两 Tab：报表/记账簿）→ iOS `TabView` + 每个 Tab 内 `NavigationStack`
- Android `ModalBottomSheet`（分类管理）→ iOS `.sheet`（内部 `NavigationStack`）

页面映射：

- `MainActivity` + `MainNavigation` → `FamilyFinanceApp` + `RootTabView`
- `MainScreen`（报表）→ `ReportScreen`
- `BillScreen`（账单）→ `BillScreen`
- `CategoryManagementScreen`（分类管理）→ `CategoryManagementScreen`
- `PdfGenerator` → `PDFGenerator`（生成）+ `QuickLookPreview`（预览）+ `ShareSheet`（分享）

## 运行方式（建议）

1. 用 Xcode 新建 `App`（SwiftUI）工程，例如命名 `FamilyFinance`
2. 将 `ios/FamilyFinance/` 下的 `Models/ Views/ ViewModels/ Services/ Utilities/` 加入工程
3. 将工程的入口 `@main` 指向 `FamilyFinanceApp`（见 `ios/FamilyFinance/FamilyFinanceApp.swift`）

## 命令行构建（推荐：直接用本仓库生成的工程）

本仓库已提供 XcodeGen 配置：`ios/project.yml`。

### 1) 生成 Xcode 工程

```bash
cd ios
./scripts/generate_xcodeproj.sh
open FamilyFinance.xcodeproj
```

首次需要在 Xcode 里设置 Signing（Team / Bundle ID），否则真机归档会报 “requires a development team”。

### 2) 导出真机安装包（IPA）

```bash
cd ios
TEAM_ID=你的TeamID BUNDLE_ID=你的BundleID ./scripts/build_ipa.sh development build/ipa
```

`development` 可按需替换为 `ad-hoc` / `app-store`（需要对应的开发者账号与证书配置）。
