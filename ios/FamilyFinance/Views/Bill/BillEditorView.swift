import SwiftUI

struct BillEditorView: View {
    enum Mode {
        case add
        case edit(Bill)

        var title: String {
            switch self {
            case .add: return "添加账单"
            case .edit: return "编辑账单"
            }
        }
    }

    let mode: Mode
    let categories: [BillCategory]
    let onSave: (Double, String, BillType, UInt64, String, String) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var amountText: String
    @State private var selectedCategoryId: String
    @State private var selectedType: BillType
    @State private var selectedColor: UInt64
    @State private var selectedDate: Date
    @State private var note: String

    @State private var showValidationAlert = false

    init(mode: Mode, categories: [BillCategory], onSave: @escaping (Double, String, BillType, UInt64, String, String) -> Void) {
        self.mode = mode
        self.categories = categories
        self.onSave = onSave

        switch mode {
        case .add:
            _amountText = State(initialValue: "")
            _selectedCategoryId = State(initialValue: "")
            _selectedType = State(initialValue: .expense)
            _selectedColor = State(initialValue: BillPresetColors.argb.first ?? 0xFF9E9E9E)
            _selectedDate = State(initialValue: Date())
            _note = State(initialValue: "")
        case .edit(let bill):
            _amountText = State(initialValue: String(bill.amount))
            _selectedCategoryId = State(initialValue: bill.categoryId)
            _selectedType = State(initialValue: bill.type)
            _selectedColor = State(initialValue: bill.color)
            _selectedDate = State(initialValue: DateUtils.date(fromISODateString: bill.date) ?? Date())
            _note = State(initialValue: bill.note)
        }
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("类型") {
                    Picker("类型", selection: $selectedType) {
                        Text("收入").tag(BillType.income)
                        Text("支出").tag(BillType.expense)
                    }
                    .pickerStyle(.segmented)
                }

                Section("金额") {
                    TextField("金额", text: $amountText)
                        .keyboardType(.decimalPad)
                }

                Section("日期") {
                    DatePicker("日期", selection: $selectedDate, displayedComponents: .date)
                }

                Section("分类") {
                    Picker("分类", selection: $selectedCategoryId) {
                        ForEach(categories) { category in
                            Text(category.name).tag(category.id)
                        }
                    }
                }

                Section("颜色") {
                    BillColorPicker(selectedColor: $selectedColor)
                        .padding(.vertical, 6)
                }

                Section("备注") {
                    TextField("备注", text: $note, axis: .vertical)
                        .lineLimit(1...3)
                }
            }
            .navigationTitle(mode.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("确定") { save() }
                }
            }
            .alert("输入不完整", isPresented: $showValidationAlert) {
                Button("确定", role: .cancel) {}
            } message: {
                Text("请填写有效金额、选择分类与日期。")
            }
            .onAppear { ensureCategorySelection() }
            .onChange(of: categories) { _ in ensureCategorySelection() }
            .onChange(of: selectedType) { _ in
                // Android 端切换类型时会清空分类选择，这里保持一致
                selectedCategoryId = ""
                ensureCategorySelection()
            }
        }
    }

    private func ensureCategorySelection() {
        guard !categories.isEmpty else { return }
        if selectedCategoryId.isEmpty || !categories.contains(where: { $0.id == selectedCategoryId }) {
            selectedCategoryId = categories[0].id
        }
    }

    private func save() {
        let amountValue = Double(amountText) ?? 0
        guard amountValue > 0, !selectedCategoryId.isEmpty else {
            showValidationAlert = true
            return
        }

        let dateString = DateUtils.isoDayString(from: selectedDate)
        onSave(amountValue, selectedCategoryId, selectedType, selectedColor, dateString, note)
        dismiss()
    }
}
