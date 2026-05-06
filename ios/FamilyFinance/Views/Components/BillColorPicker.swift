import SwiftUI

struct BillColorPicker: View {
    @Binding var selectedColor: UInt64

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("选择颜色")
                .font(.footnote)
                .foregroundStyle(.secondary)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(BillPresetColors.argb, id: \.self) { color in
                        let isSelected = color == selectedColor
                        Circle()
                            .fill(Color(argb: color))
                            .frame(width: 36, height: 36)
                            .overlay(
                                Circle()
                                    .strokeBorder(isSelected ? Color.accentColor : .clear, lineWidth: 3)
                            )
                            .onTapGesture { selectedColor = color }
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }
}

