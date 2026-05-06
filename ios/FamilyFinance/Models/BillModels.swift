import Foundation

enum BillType: String, Codable, CaseIterable, Sendable {
    case income = "INCOME"
    case expense = "EXPENSE"
}

struct BillCategory: Identifiable, Codable, Hashable, Sendable {
    var id: String
    var name: String
    var type: BillType
}

struct Bill: Identifiable, Codable, Hashable, Sendable {
    var id: String
    var amount: Double
    var categoryId: String
    var type: BillType
    /// ARGB, e.g. `0xFF9E9E9E`
    var color: UInt64 = 0xFF9E9E9E
    /// `yyyy-MM-dd`
    var date: String
    var note: String = ""
    var createdAt: Int64 = DateUtils.nowMillis()
}

struct BillBookData: Codable, Equatable, Sendable {
    var categories: [BillCategory] = []
    var bills: [Bill] = []

    static func `default`() -> BillBookData {
        let defaultCategories: [BillCategory] = [
            .init(id: "cat_income_salary", name: "工资收入", type: .income),
            .init(id: "cat_income_bonus", name: "奖金", type: .income),
            .init(id: "cat_income_other", name: "其他收入", type: .income),
            .init(id: "cat_expense_food", name: "餐饮美食", type: .expense),
            .init(id: "cat_expense_transport", name: "交通出行", type: .expense),
            .init(id: "cat_expense_shopping", name: "购物消费", type: .expense),
            .init(id: "cat_expense_entertainment", name: "娱乐休闲", type: .expense),
            .init(id: "cat_expense_medical", name: "医疗健康", type: .expense),
            .init(id: "cat_expense_education", name: "教育学习", type: .expense),
            .init(id: "cat_expense_family", name: "亲子", type: .expense),
            .init(id: "cat_expense_other", name: "其他支出", type: .expense),
        ]
        return BillBookData(categories: defaultCategories, bills: [])
    }

    func addingCategory(_ category: BillCategory) -> BillBookData {
        guard !categories.contains(where: { $0.id == category.id }) else { return self }
        var next = self
        next.categories.append(category)
        return next
    }

    func updatingCategory(_ categoryId: String, updater: (BillCategory) -> BillCategory) -> BillBookData {
        var next = self
        next.categories = categories.map { $0.id == categoryId ? updater($0) : $0 }
        return next
    }

    func removingCategory(_ categoryId: String) -> BillBookData {
        var next = self
        next.categories = categories.filter { $0.id != categoryId }
        next.bills = bills.filter { $0.categoryId != categoryId }
        return next
    }

    func addingBill(_ bill: Bill) -> BillBookData {
        guard !bills.contains(where: { $0.id == bill.id }) else { return self }
        var next = self
        next.bills.append(bill)
        return next
    }

    func updatingBill(_ billId: String, updater: (Bill) -> Bill) -> BillBookData {
        var next = self
        next.bills = bills.map { $0.id == billId ? updater($0) : $0 }
        return next
    }

    func removingBill(_ billId: String) -> BillBookData {
        var next = self
        next.bills = bills.filter { $0.id != billId }
        return next
    }

    func category(for categoryId: String) -> BillCategory? {
        categories.first(where: { $0.id == categoryId })
    }

    func categories(of type: BillType) -> [BillCategory] {
        categories.filter { $0.type == type }
    }

    func bills(on date: String) -> [Bill] {
        bills
            .filter { $0.date == date }
            .sorted(by: { $0.createdAt > $1.createdAt })
    }

    func bills(in startDate: String, _ endDate: String) -> [Bill] {
        bills
            .filter { $0.date >= startDate && $0.date <= endDate }
            .sorted(by: { $0.createdAt > $1.createdAt })
    }

    func bills(categoryId: String) -> [Bill] {
        bills
            .filter { $0.categoryId == categoryId }
            .sorted(by: { $0.createdAt > $1.createdAt })
    }

    func totalIncome(on date: String) -> Double {
        bills.filter { $0.date == date && $0.type == .income }.reduce(0) { $0 + $1.amount }
    }

    func totalExpense(on date: String) -> Double {
        bills.filter { $0.date == date && $0.type == .expense }.reduce(0) { $0 + $1.amount }
    }

    func totalIncome(in startDate: String, _ endDate: String) -> Double {
        bills.filter { $0.date >= startDate && $0.date <= endDate && $0.type == .income }.reduce(0) { $0 + $1.amount }
    }

    func totalExpense(in startDate: String, _ endDate: String) -> Double {
        bills.filter { $0.date >= startDate && $0.date <= endDate && $0.type == .expense }.reduce(0) { $0 + $1.amount }
    }

    func filterBills(
        categoryId: String? = nil,
        type: BillType? = nil,
        color: UInt64? = nil,
        startDate: String? = nil,
        endDate: String? = nil,
        minAmount: Double? = nil,
        maxAmount: Double? = nil
    ) -> [Bill] {
        bills
            .filter { bill in
                (categoryId == nil || bill.categoryId == categoryId) &&
                (type == nil || bill.type == type) &&
                (color == nil || bill.color == color) &&
                (startDate == nil || bill.date >= startDate!) &&
                (endDate == nil || bill.date <= endDate!) &&
                (minAmount == nil || bill.amount >= minAmount!) &&
                (maxAmount == nil || bill.amount <= maxAmount!)
            }
            .sorted(by: { $0.createdAt > $1.createdAt })
    }
}

