import SwiftUI

struct RootTabView: View {
    var body: some View {
        TabView {
            NavigationStack {
                ReportScreen()
            }
            .tabItem {
                Label("财务报表", systemImage: "chart.bar.doc.horizontal")
            }

            NavigationStack {
                BillScreen()
            }
            .tabItem {
                Label("记账簿", systemImage: "receipt")
            }
        }
    }
}

