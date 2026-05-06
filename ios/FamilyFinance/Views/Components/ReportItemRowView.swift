import SwiftUI

struct ReportItemRowView: View {
    let item: ReportItem
    let period: String
    let depth: Int
    let showNotes: Bool

    let onValueChange: (Double) -> Void
    let onNoteChange: (String) -> Void
    let onRename: () -> Void
    let onAddSubItem: () -> Void
    let onDelete: () -> Void

    @State private var valueText: String
    @State private var noteText: String

    init(
        item: ReportItem,
        period: String,
        depth: Int,
        showNotes: Bool,
        onValueChange: @escaping (Double) -> Void,
        onNoteChange: @escaping (String) -> Void,
        onRename: @escaping () -> Void,
        onAddSubItem: @escaping () -> Void,
        onDelete: @escaping () -> Void
    ) {
        self.item = item
        self.period = period
        self.depth = depth
        self.showNotes = showNotes
        self.onValueChange = onValueChange
        self.onNoteChange = onNoteChange
        self.onRename = onRename
        self.onAddSubItem = onAddSubItem
        self.onDelete = onDelete

        let displayValue = item.value(for: period)
        let initialValueText: String = {
            guard displayValue != 0 else { return "" }
            if displayValue.rounded(.towardZero) == displayValue {
                return String(Int64(displayValue))
            }
            return String(displayValue)
        }()

        _valueText = State(initialValue: initialValueText)
        _noteText = State(initialValue: item.note ?? "")
    }

    var body: some View {
        HStack(spacing: 12) {
            HStack(spacing: 8) {
                if depth > 0 {
                    Circle()
                        .fill(.secondary.opacity(0.35))
                        .frame(width: 6, height: 6)
                }

                Text(item.name)
                    .font(.body.weight(.medium))
                    .lineLimit(1)
                    .truncationMode(.tail)
            }
            .padding(.leading, CGFloat(depth) * 16)
            .contentShape(Rectangle())
            .onTapGesture(count: 2, perform: onRename)
            .contextMenu {
                Button("重命名", action: onRename)
                Button("新增子科目", action: onAddSubItem)
                Button("删除", role: .destructive, action: onDelete)
            }

            if showNotes {
                TextField("备注…", text: $noteText)
                    .textFieldStyle(.roundedBorder)
                    .onChange(of: noteText) { newValue in
                        onNoteChange(newValue)
                    }
                    .frame(minWidth: 120)
            }

            if item.hasChildren {
                Text("¥ \(Formatters.formatNumber(item.value(for: period)))")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .frame(minWidth: 80, alignment: .trailing)
            } else {
                TextField("0", text: $valueText)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numbersAndPunctuation)
                    .multilineTextAlignment(.trailing)
                    .frame(minWidth: 80, alignment: .trailing)
                    .onChange(of: valueText) { newValue in
                        let filtered = newValue.filter { $0.isNumber || $0 == "." || $0 == "-" }
                        if filtered != newValue {
                            valueText = filtered
                        }
                        if let value = Double(filtered) {
                            onValueChange(value)
                        }
                    }
            }

            HStack(spacing: 8) {
                Button(action: onAddSubItem) {
                    Image(systemName: "plus.square.on.square")
                        .font(.system(size: 16, weight: .regular))
                }
                .buttonStyle(.borderless)

                Button(role: .destructive, action: onDelete) {
                    Image(systemName: "trash")
                        .font(.system(size: 16, weight: .regular))
                }
                .buttonStyle(.borderless)
            }
            .foregroundStyle(.secondary)
        }
        .onChange(of: item.note ?? "") { newValue in
            if newValue != noteText { noteText = newValue }
        }
        .onChange(of: item.value(for: period)) { newValue in
            let nextText: String = {
                guard newValue != 0 else { return "" }
                if newValue.rounded(.towardZero) == newValue {
                    return String(Int64(newValue))
                }
                return String(newValue)
            }()
            if nextText != valueText { valueText = nextText }
        }
    }
}
