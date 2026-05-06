import SwiftUI

struct SummaryCardView: View {
    let title: String
    let value: Double
    let accentColor: Color
    var systemImage: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(.footnote)
                .foregroundStyle(.secondary)

            HStack(alignment: .lastTextBaseline) {
                Text("¥ \(Formatters.formatNumber(value))")
                    .font(.title3.weight(.semibold))
                    .foregroundStyle(accentColor)

                Spacer()

                if let systemImage {
                    Image(systemName: systemImage)
                        .foregroundStyle(accentColor.opacity(0.8))
                }
            }
        }
        .padding(16)
        .background(.background, in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(.quaternary, lineWidth: 1)
        )
    }
}

