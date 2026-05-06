import Foundation

enum CategoryType: String, Codable, CaseIterable, Sendable {
    case income = "INCOME"
    case expense = "EXPENSE"
    case asset = "ASSET"
    case liability = "LIABILITY"

    static func fromString(_ value: String) -> CategoryType {
        switch value.lowercased() {
        case "income": return .income
        case "expense": return .expense
        case "asset": return .asset
        case "liability": return .liability
        default: return .asset
        }
    }
}

enum ViewMode: String, Codable, CaseIterable, Sendable {
    case year = "YEAR"
    case month = "MONTH"
    case day = "DAY"
}

struct Summary: Codable, Equatable, Sendable {
    var income: Double = 0
    var expense: Double = 0
    var asset: Double = 0
    var liability: Double = 0
    var cashflow: Double = 0
    var netWorth: Double = 0
}

struct NoteItem: Identifiable, Codable, Hashable, Sendable {
    var id: String
    var label: String
    var value: String
}

struct ReportItem: Identifiable, Codable, Hashable, Sendable {
    var id: String
    var name: String
    var values: [String: Double] = [:]
    var quantities: [String: Double] = [:]
    var unitPrices: [String: Double] = [:]
    var note: String? = nil
    var children: [ReportItem]? = nil

    var hasChildren: Bool { !(children ?? []).isEmpty }

    func value(for period: String) -> Double {
        if let children, !children.isEmpty {
            return children.reduce(0) { $0 + $1.value(for: period) }
        }
        return values[period] ?? 0
    }

    func updatingValue(for period: String, to newValue: Double) -> ReportItem {
        let qty = quantities[period] ?? 1.0
        var next = self
        next.values[period] = newValue
        next.unitPrices[period] = (qty != 0) ? (newValue / qty) : 0
        return next
    }

    func updatingNote(_ newNote: String) -> ReportItem {
        var next = self
        next.note = newNote
        return next
    }

    func renamed(_ newName: String) -> ReportItem {
        var next = self
        next.name = newName
        return next
    }

    func addingChild(_ child: ReportItem) -> ReportItem {
        var next = self
        var nextChildren = next.children ?? []
        nextChildren.append(child)
        next.children = nextChildren
        return next
    }

    func updatingChild(_ targetId: String, updater: (ReportItem) -> ReportItem) -> ReportItem {
        if id == targetId {
            return updater(self)
        }
        guard let children else { return self }
        var next = self
        next.children = children.map { $0.updatingChild(targetId, updater: updater) }
        return next
    }

    func removingChild(_ targetId: String) -> ReportItem {
        guard let children else { return self }
        var next = self
        next.children = children
            .filter { $0.id != targetId }
            .map { $0.removingChild(targetId) }
        return next
    }
}

struct CategoryGroup: Identifiable, Codable, Hashable, Sendable {
    var id: String
    var type: CategoryType
    var name: String
    var items: [ReportItem] = []

    func total(for period: String) -> Double {
        items.reduce(0) { $0 + $1.value(for: period) }
    }

    func addingItem(_ item: ReportItem) -> CategoryGroup {
        var next = self
        next.items.append(item)
        return next
    }

    func updatingItem(_ itemId: String, updater: (ReportItem) -> ReportItem) -> CategoryGroup {
        var next = self
        next.items = items.map { $0.updatingChild(itemId, updater: updater) }
        return next
    }

    func removingItem(_ itemId: String) -> CategoryGroup {
        var next = self
        next.items = items
            .filter { $0.id != itemId }
            .map { $0.removingChild(itemId) }
        return next
    }

    func addingSubItem(parentId: String, child: ReportItem) -> CategoryGroup {
        updatingItem(parentId) { $0.addingChild(child) }
    }
}

struct ReportData: Codable, Equatable, Sendable {
    var years: [String] = []
    var months: [String] = []
    var days: [String] = []

    /// Legacy / compatibility bucket (Android 端保留了 `groups` 字段)
    var groups: [CategoryGroup] = []
    var flowGroups: [CategoryGroup] = []
    var balanceGroups: [CategoryGroup] = []
    var notes: [NoteItem] = []

    static func `default`() -> ReportData {
        let defaultYears = ["2025"]
        let defaultMonths = ["2025-01", "2025-02"]
        let defaultDays = ["2025-01-01"]

        let flowGroups: [CategoryGroup] = [
            CategoryGroup(
                id: "income",
                type: .income,
                name: "收入 (Income)",
                items: [
                    ReportItem(
                        id: "inc_1",
                        name: "工资收入",
                        values: ["2025": 240_000, "2025-01": 20_000, "2025-02": 20_000]
                    ),
                    ReportItem(
                        id: "inc_2",
                        name: "公积金收入",
                        values: ["2025": 6_000, "2025-02": 500]
                    ),
                    ReportItem(id: "inc_3", name: "股票投资收入"),
                    ReportItem(id: "inc_4", name: "期货投资收入"),
                    ReportItem(id: "inc_5", name: "黄金投资收入"),
                    ReportItem(id: "inc_6", name: "白银投资收入"),
                    ReportItem(id: "inc_7", name: "铑投资收入"),
                    ReportItem(id: "inc_8", name: "加密货币投资收入"),
                    ReportItem(id: "inc_9", name: "其他投资收入"),
                ]
            ),
            CategoryGroup(
                id: "expense",
                type: .expense,
                name: "支出 (Expenses)",
                items: [
                    ReportItem(
                        id: "exp_1",
                        name: "房贷/房租",
                        values: ["2025": 60_000, "2025-01": 5_000, "2025-02": 5_000]
                    ),
                    ReportItem(
                        id: "exp_2",
                        name: "餐饮美食",
                        values: ["2025": 36_000, "2025-01": 3_000, "2025-02": 2_800]
                    ),
                    ReportItem(
                        id: "exp_3",
                        name: "交通出行",
                        values: ["2025": 9_600, "2025-02": 800]
                    ),
                ]
            ),
        ]

        let balanceGroups: [CategoryGroup] = [
            CategoryGroup(
                id: "asset",
                type: .asset,
                name: "资产 (Assets)",
                items: [
                    ReportItem(id: "ast_cash", name: "现金", values: ["2025-01-01": 20_000]),
                    ReportItem(id: "ast_recv", name: "应收借款", values: ["2025-01-01": 50_000]),
                    ReportItem(id: "ast_int", name: "累计利息", values: ["2025-01-01": 500]),
                    ReportItem(id: "ast_gold", name: "黄金资产", values: ["2025-01-01": 50_000]),
                    ReportItem(id: "ast_stock", name: "股票资产", values: ["2025-01-01": 110_000]),
                    ReportItem(id: "ast_rhod", name: "铑实物", values: ["2025-01-01": 30_000]),
                    ReportItem(id: "ast_crypto", name: "加密货币", values: ["2025-01-01": 38_000]),
                    ReportItem(id: "ast_fund", name: "公积金", values: ["2025-01-01": 78_000]),
                    ReportItem(id: "ast_savegold", name: "积存金", values: ["2025-01-01": 15_000]),
                    ReportItem(id: "ast_futures", name: "期货资产", values: ["2025-01-01": 24_000]),
                ]
            ),
            CategoryGroup(
                id: "liability",
                type: .liability,
                name: "负债 (Liabilities)",
                items: [
                    ReportItem(id: "lia_card", name: "信用卡未还", values: ["2025-01-01": 2_500]),
                    ReportItem(id: "lia_debt", name: "欠款", values: ["2025-01-01": 50_000]),
                ]
            ),
        ]

        return ReportData(
            years: defaultYears,
            months: defaultMonths,
            days: defaultDays,
            groups: flowGroups + balanceGroups,
            flowGroups: flowGroups,
            balanceGroups: balanceGroups,
            notes: [
                NoteItem(id: "note_1", label: "养老保险总缴纳额", value: "200000"),
            ]
        )
    }

    func summary(for viewMode: ViewMode, period: String) -> Summary {
        let targetGroups: [CategoryGroup] = {
            switch viewMode {
            case .day:
                return !balanceGroups.isEmpty ? balanceGroups : groups.filter { $0.type == .asset || $0.type == .liability }
            case .year, .month:
                return !flowGroups.isEmpty ? flowGroups : groups.filter { $0.type == .income || $0.type == .expense }
            }
        }()

        var income = 0.0
        var expense = 0.0
        var asset = 0.0
        var liability = 0.0

        for group in targetGroups {
            let total = group.total(for: period)
            switch group.type {
            case .income: income += total
            case .expense: expense += total
            case .asset: asset += total
            case .liability: liability += total
            }
        }

        return Summary(
            income: income,
            expense: expense,
            asset: asset,
            liability: liability,
            cashflow: income - expense,
            netWorth: asset - liability
        )
    }

    func displayGroups(for viewMode: ViewMode) -> [CategoryGroup] {
        switch viewMode {
        case .day:
            return !balanceGroups.isEmpty ? balanceGroups : groups.filter { $0.type == .asset || $0.type == .liability }
        case .year, .month:
            return !flowGroups.isEmpty ? flowGroups : groups.filter { $0.type == .income || $0.type == .expense }
        }
    }

    func updatingGroup(_ groupId: String, updater: (CategoryGroup) -> CategoryGroup) -> ReportData {
        func update(_ list: [CategoryGroup]) -> [CategoryGroup] {
            list.map { $0.id == groupId ? updater($0) : $0 }
        }
        var next = self
        next.flowGroups = update(flowGroups)
        next.balanceGroups = update(balanceGroups)
        next.groups = update(groups)
        return next
    }

    func addingDay(_ day: String) -> ReportData {
        guard !days.contains(day) else { return self }
        var next = self
        next.days.append(day)
        return next
    }

    func addingMonth(_ month: String) -> ReportData {
        guard !months.contains(month) else { return self }
        var next = self
        next.months.append(month)
        return next
    }

    func addingYear(_ year: String) -> ReportData {
        guard !years.contains(year) else { return self }
        var next = self
        next.years.append(year)
        return next
    }

    func addingNote(_ note: NoteItem) -> ReportData {
        var next = self
        next.notes.append(note)
        return next
    }

    func updatingNote(_ noteId: String, updater: (NoteItem) -> NoteItem) -> ReportData {
        var next = self
        next.notes = notes.map { $0.id == noteId ? updater($0) : $0 }
        return next
    }

    func removingNote(_ noteId: String) -> ReportData {
        var next = self
        next.notes = notes.filter { $0.id != noteId }
        return next
    }
}

