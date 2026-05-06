import SwiftUI

struct CategoryGroupCard: View {
    let group: CategoryGroup
    let period: String
    let showNotes: Bool

    let onAddItem: () -> Void
    let onValueChange: (String, Double) -> Void
    let onNoteChange: (String, String) -> Void
    let onRenameItem: (String) -> Void
    let onAddSubItem: (String) -> Void
    let onDeleteItem: (String) -> Void

    private var accentColor: Color {
        switch group.type {
        case .income: return Theme.Colors.income
        case .expense: return Theme.Colors.expense
        case .asset: return Theme.Colors.asset
        case .liability: return Theme.Colors.liability
        }
    }

    private var categoryLabel: String {
        switch group.type {
        case .income: return "收入"
        case .expense: return "支出"
        case .asset: return "资产"
        case .liability: return "负债"
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(categoryLabel)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(accentColor)
                    Text("提示：双击/长按可重命名")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                Button(action: onAddItem) {
                    Label("新增", systemImage: "plus")
                }
                .buttonStyle(.bordered)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(accentColor.opacity(0.12))

            headerRow
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(.secondary.opacity(0.06))

            VStack(spacing: 0) {
                ForEach(group.items) { item in
                    ReportItemTreeView(
                        item: item,
                        period: period,
                        depth: 0,
                        showNotes: showNotes,
                        onValueChange: onValueChange,
                        onNoteChange: onNoteChange,
                        onRename: onRenameItem,
                        onAddSubItem: onAddSubItem,
                        onDelete: onDeleteItem
                    )
                }
            }

            HStack {
                Text("小计")
                    .font(.body.weight(.semibold))
                Spacer()
                Text("¥ \(Formatters.formatNumber(group.total(for: period)))")
                    .font(.body.weight(.semibold))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(.secondary.opacity(0.05))
        }
        .background(.background, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(.quaternary, lineWidth: 1)
        )
    }

    @ViewBuilder
    private var headerRow: some View {
        HStack(spacing: 12) {
            Text("科目")
                .font(.caption.weight(.semibold))
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity, alignment: .leading)

            if showNotes {
                Text("备注")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }

            Text(period)
                .font(.caption.weight(.semibold))
                .foregroundStyle(.secondary)
                .frame(minWidth: 80, alignment: .trailing)

            Text("操作")
                .font(.caption.weight(.semibold))
                .foregroundStyle(.secondary)
                .frame(minWidth: 60, alignment: .trailing)
        }
    }
}

