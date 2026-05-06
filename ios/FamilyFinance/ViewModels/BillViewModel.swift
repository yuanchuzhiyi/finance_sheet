import Foundation

@MainActor
final class BillViewModel: ObservableObject {
    @Published private(set) var billBookData: BillBookData = .default()

    @Published var selectedDate: String = DateUtils.isoDayString(from: Date())
    @Published var selectedBillType: BillType? = nil

    @Published var filterCategoryId: String? = nil
    @Published var filterColor: UInt64? = nil
    @Published var filterStartDate: String? = nil
    @Published var filterEndDate: String? = nil
    @Published var filterMinAmount: Double? = nil
    @Published var filterMaxAmount: Double? = nil

    private let repository: BillRepository

    init(repository: BillRepository = BillRepository()) {
        self.repository = repository
        Task { await load() }
    }

    var currentBills: [Bill] {
        billBookData.filterBills(
            categoryId: filterCategoryId,
            type: selectedBillType,
            color: filterColor,
            startDate: filterStartDate,
            endDate: filterEndDate,
            minAmount: filterMinAmount,
            maxAmount: filterMaxAmount
        )
    }

    var currentIncome: Double {
        currentBills.filter { $0.type == .income }.reduce(0) { $0 + $1.amount }
    }

    var currentExpense: Double {
        currentBills.filter { $0.type == .expense }.reduce(0) { $0 + $1.amount }
    }

    var currentBalance: Double {
        currentIncome - currentExpense
    }

    var filteredCategories: [BillCategory] {
        guard let selectedBillType else { return billBookData.categories }
        return billBookData.categories(of: selectedBillType)
    }

    var hasActiveFilters: Bool {
        selectedBillType != nil ||
        filterCategoryId != nil ||
        filterColor != nil ||
        filterStartDate != nil ||
        filterEndDate != nil ||
        filterMinAmount != nil ||
        filterMaxAmount != nil
    }

    func load() async {
        let data = await repository.loadBillBookData()
        billBookData = data
    }

    func save() {
        Task { try? await repository.saveBillBookData(billBookData) }
    }

    func resetToDefault() {
        billBookData = .default()
        save()
    }

    func setSelectedDate(_ date: String) {
        selectedDate = date
    }

    func setSelectedBillType(_ type: BillType?) {
        selectedBillType = type
    }

    func setFilterCategory(_ categoryId: String?) {
        filterCategoryId = categoryId
    }

    func setFilterColor(_ color: UInt64?) {
        filterColor = color
    }

    func setFilterDateRange(startDate: String?, endDate: String?) {
        filterStartDate = startDate
        filterEndDate = endDate
    }

    func setFilterAmountRange(minAmount: Double?, maxAmount: Double?) {
        filterMinAmount = minAmount
        filterMaxAmount = maxAmount
    }

    func clearFilters() {
        selectedBillType = nil
        filterCategoryId = nil
        filterColor = nil
        filterStartDate = nil
        filterEndDate = nil
        filterMinAmount = nil
        filterMaxAmount = nil
    }

    func addBill(amount: Double, categoryId: String, type: BillType, color: UInt64, date: String, note: String = "") {
        let bill = Bill(
            id: "bill_\(DateUtils.nowMillis())",
            amount: amount,
            categoryId: categoryId,
            type: type,
            color: color,
            date: date,
            note: note
        )
        billBookData = billBookData.addingBill(bill)
        save()
    }

    func updateBill(
        billId: String,
        amount: Double? = nil,
        categoryId: String? = nil,
        type: BillType? = nil,
        color: UInt64? = nil,
        date: String? = nil,
        note: String? = nil
    ) {
        billBookData = billBookData.updatingBill(billId) { bill in
            var next = bill
            if let amount { next.amount = amount }
            if let categoryId { next.categoryId = categoryId }
            if let type { next.type = type }
            if let color { next.color = color }
            if let date { next.date = date }
            if let note { next.note = note }
            return next
        }
        save()
    }

    func deleteBill(_ billId: String) {
        billBookData = billBookData.removingBill(billId)
        save()
    }

    func addCategory(name: String, type: BillType) {
        let category = BillCategory(
            id: "cat_\(DateUtils.nowMillis())",
            name: name,
            type: type
        )
        billBookData = billBookData.addingCategory(category)
        save()
    }

    func updateCategory(categoryId: String, name: String? = nil, type: BillType? = nil) {
        billBookData = billBookData.updatingCategory(categoryId) { category in
            var next = category
            if let name { next.name = name }
            if let type { next.type = type }
            return next
        }
        save()
    }

    func deleteCategory(_ categoryId: String) {
        billBookData = billBookData.removingCategory(categoryId)
        save()
    }
}

