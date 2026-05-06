import SwiftUI

struct CategoryEditorView: View {
    enum Mode {
        case add
        case edit(BillCategory)

        var title: String {
            switch self {
            case .add: return "添加分类"
            case .edit: return "编辑分类"
            }
        }
    }

    let mode: Mode
    let onSave: (String, BillType) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var name: String
    @State private var type: BillType
    @State private var showValidationAlert = false

    init(mode: Mode, onSave: @escaping (String, BillType) -> Void) {
        self.mode = mode
        self.onSave = onSave

        switch mode {
        case .add:
            _name = State(initialValue: "")
            _type = State(initialValue: .expense)
        case .edit(let category):
            _name = State(initialValue: category.name)
            _type = State(initialValue: category.type)
        }
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("分类名称") {
                    TextField("名称", text: $name)
                }

                Section("类型") {
                    Picker("类型", selection: $type) {
                        Text("收入").tag(BillType.income)
                        Text("支出").tag(BillType.expense)
                    }
                    .pickerStyle(.segmented)
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
            .alert("请输入名称", isPresented: $showValidationAlert) {
                Button("确定", role: .cancel) {}
            }
        }
    }

    private func save() {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else {
            showValidationAlert = true
            return
        }
        onSave(trimmed, type)
        dismiss()
    }
}

