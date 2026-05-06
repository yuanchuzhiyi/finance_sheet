import SwiftUI

@main
struct FamilyFinanceApp: App {
    @StateObject private var reportViewModel = ReportViewModel()
    @StateObject private var billViewModel = BillViewModel()

    var body: some Scene {
        WindowGroup {
            RootTabView()
                .environmentObject(reportViewModel)
                .environmentObject(billViewModel)
        }
    }
}

