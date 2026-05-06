import SwiftUI

struct PeriodMenu: View {
    let periods: [String]
    @Binding var selection: String

    var body: some View {
        Menu {
            ForEach(periods, id: \.self) { period in
                Button(period) { selection = period }
            }
        } label: {
            Label(selection.isEmpty ? "选择" : selection, systemImage: "chevron.down")
                .labelStyle(.titleAndIcon)
        }
        .buttonStyle(.bordered)
        .disabled(periods.isEmpty)
    }
}

