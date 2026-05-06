import Foundation

actor ReportRepository {
    private let store = JSONFileStore(fileURL: AppPaths.reportDataURL)

    func loadReportData() async -> ReportData {
        await store.load(default: ReportData.default())
    }

    func saveReportData(_ data: ReportData) async throws {
        try await store.save(data)
    }

    func resetToDefault() async throws {
        try await store.save(ReportData.default())
    }

    /// Android 端删除报表等价于重置模板
    func deleteReport() async throws {
        try await resetToDefault()
    }
}
