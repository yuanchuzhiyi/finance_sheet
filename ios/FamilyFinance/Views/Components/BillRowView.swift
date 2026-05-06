import SwiftUI

struct BillRowView: View {
    let bill: Bill
    let category: BillCategory?
    var onEdit: (() -> Void)? = nil
    var onDelete: (() -> Void)? = nil

    private var billColor: Color { Color(argb: bill.color) }
    private var amountColor: Color { bill.type == .income ? .green : .red }

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(billColor)
                .frame(width: 10, height: 10)

            VStack(alignment: .leading, spacing: 4) {
                Text(category?.name ?? "未知分类")
                    .font(.body.weight(.medium))
                if !bill.note.isEmpty {
                    Text(bill.note)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            HStack(spacing: 8) {
                Text("¥\(Formatters.formatNumber(bill.amount))")
                    .font(.body.weight(.semibold))
                    .foregroundStyle(amountColor)

                if let onEdit {
                    Button(action: onEdit) {
                        Image(systemName: "pencil")
                            .font(.system(size: 16, weight: .regular))
                    }
                    .buttonStyle(.borderless)
                    .foregroundStyle(.secondary)
                }

                if let onDelete {
                    Button(role: .destructive, action: onDelete) {
                        Image(systemName: "trash")
                            .font(.system(size: 16, weight: .regular))
                    }
                    .buttonStyle(.borderless)
                }
            }
        }
        .padding(12)
        .background(.background, in: RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .strokeBorder(billColor, lineWidth: 2)
        )
    }
}
