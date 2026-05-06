import SwiftUI

struct ViewModePicker: View {
    @Binding var selection: ViewMode

    var body: some View {
        Picker("查看维度", selection: $selection) {
            Text("年").tag(ViewMode.year)
            Text("月").tag(ViewMode.month)
            Text("日").tag(ViewMode.day)
        }
        .pickerStyle(.segmented)
    }
}

