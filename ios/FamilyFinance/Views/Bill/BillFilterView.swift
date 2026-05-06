import SwiftUI

struct BillFilterView: View {
    let categories: [BillCategory]
    let initialCategoryId: String?
    let initialType: BillType?
    let initialColor: UInt64?
    let initialStartDate: String?
    let initialEndDate: String?
    let initialMinAmount: Double?
    let initialMaxAmount: Double?

    let onApply: (String?, BillType?, UInt64?, String?, String?, Double?, Double?) -> Void
    let onClear: () -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var categoryId: String?
    @State private var type: BillType?
    @State private var color: UInt64?

    @State private var startEnabled: Bool
    @State private var endEnabled: Bool
    @State private var startDate: Date
    @State private var endDate: Date

    @State private var minAmountText: String
    @State private var maxAmountText: String

    init(
        categories: [BillCategory],
        selectedCategoryId: String?,
        selectedType: BillType?,
        selectedColor: UInt64?,
        startDate: String?,
        endDate: String?,
        minAmount: Double?,
        maxAmount: Double?,
        onApply: @escaping (String?, BillType?, UInt64?, String?, String?, Double?, Double?) -> Void,
        onClear: @escaping () -> Void
    ) {
        self.categories = categories
        self.initialCategoryId = selectedCategoryId
        self.initialType = selectedType
        self.initialColor = selectedColor
        self.initialStartDate = startDate
        self.initialEndDate = endDate
        self.initialMinAmount = minAmount
        self.initialMaxAmount = maxAmount
        self.onApply = onApply
        self.onClear = onClear

        _categoryId = State(initialValue: selectedCategoryId)
        _type = State(initialValue: selectedType)
        _color = State(initialValue: selectedColor)

        let parsedStart = startDate.flatMap { DateUtils.date(fromISODateString: $0) }
        let parsedEnd = endDate.flatMap { DateUtils.date(fromISODateString: $0) }
        _startEnabled = State(initialValue: parsedStart != nil)
        _endEnabled = State(initialValue: parsedEnd != nil)
        _startDate = State(initialValue: parsedStart ?? Date())
        _endDate = State(initialValue: parsedEnd ?? Date())

        _minAmountText = State(initialValue: minAmount.map { String($0) } ?? "")
        _maxAmountText = State(initialValue: maxAmount.map { String($0) } ?? "")
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("类型") {
                    Picker("类型", selection: $type) {
                        Text("全部").tag(nil as BillType?)
                        Text("收入").tag(BillType.income as BillType?)
                        Text("支出").tag(BillType.expense as BillType?)
                    }
                    .pickerStyle(.segmented)
                }

                Section("分类") {
                    Picker("分类", selection: $categoryId) {
                        Text("全部").tag(nil as String?)
                        ForEach(categories) { cat in
                            Text(cat.name).tag(cat.id as String?)
                        }
                    }
                }

                Section("颜色") {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            allColorChip
                            ForEach(BillPresetColors.argb, id: \.self) { preset in
                                let isSelected = color == preset
                                Circle()
                                    .fill(Color(argb: preset))
                                    .frame(width: 36, height: 36)
                                    .overlay(
                                        Circle()
                                            .strokeBorder(isSelected ? Color.accentColor : .clear, lineWidth: 3)
                                    )
                                    .onTapGesture {
                                        color = isSelected ? nil : preset
                                    }
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }

                Section("日期范围") {
                    Toggle("开始日期", isOn: $startEnabled)
                    if startEnabled {
                        DatePicker("开始", selection: $startDate, displayedComponents: .date)
                    }

                    Toggle("结束日期", isOn: $endEnabled)
                    if endEnabled {
                        DatePicker("结束", selection: $endDate, displayedComponents: .date)
                    }
                }

                Section("金额范围") {
                    TextField("最小金额", text: $minAmountText)
                        .keyboardType(.decimalPad)
                    TextField("最大金额", text: $maxAmountText)
                        .keyboardType(.decimalPad)
                }
            }
            .navigationTitle("筛选账单")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("确定") { apply() }
                }
                ToolbarItem(placement: .bottomBar) {
                    Button("清除") { clear() }
                }
            }
        }
    }

    private var allColorChip: some View {
        let isAllSelected = color == nil
        return ZStack {
            Circle()
                .fill(.secondary.opacity(0.12))
                .frame(width: 36, height: 36)
                .overlay(
                    Circle()
                        .strokeBorder(isAllSelected ? Color.accentColor : .clear, lineWidth: 3)
                )
            Text("全")
                .font(.caption2.weight(.semibold))
                .foregroundStyle(isAllSelected ? Color.accentColor : .secondary)
        }
        .onTapGesture { color = nil }
    }

    private func clear() {
        categoryId = nil
        type = nil
        color = nil
        startEnabled = false
        endEnabled = false
        minAmountText = ""
        maxAmountText = ""
        onClear()
    }

    private func apply() {
        let startString = startEnabled ? DateUtils.isoDayString(from: startDate) : nil
        let endString = endEnabled ? DateUtils.isoDayString(from: endDate) : nil
        let min = Double(minAmountText)
        let max = Double(maxAmountText)

        onApply(categoryId, type, color, startString, endString, min, max)
        dismiss()
    }
}
