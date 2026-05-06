import SwiftUI

struct ReportItemTreeView: View {
    let item: ReportItem
    let period: String
    let depth: Int
    let showNotes: Bool

    let onValueChange: (String, Double) -> Void
    let onNoteChange: (String, String) -> Void
    let onRename: (String) -> Void
    let onAddSubItem: (String) -> Void
    let onDelete: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            ReportItemRowView(
                item: item,
                period: period,
                depth: depth,
                showNotes: showNotes,
                onValueChange: { onValueChange(item.id, $0) },
                onNoteChange: { onNoteChange(item.id, $0) },
                onRename: { onRename(item.id) },
                onAddSubItem: { onAddSubItem(item.id) },
                onDelete: { onDelete(item.id) }
            )

            if let children = item.children, !children.isEmpty {
                Divider().opacity(0.4)
                ForEach(children) { child in
                    ReportItemTreeView(
                        item: child,
                        period: period,
                        depth: depth + 1,
                        showNotes: showNotes,
                        onValueChange: onValueChange,
                        onNoteChange: onNoteChange,
                        onRename: onRename,
                        onAddSubItem: onAddSubItem,
                        onDelete: onDelete
                    )
                    Divider().opacity(0.4)
                }
            }
        }
    }
}

