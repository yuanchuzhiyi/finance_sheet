import Foundation

actor BillRepository {
    private let store = JSONFileStore(fileURL: AppPaths.billBookDataURL)

    func loadBillBookData() async -> BillBookData {
        await store.load(default: BillBookData.default())
    }

    func saveBillBookData(_ data: BillBookData) async throws {
        try await store.save(data)
    }

    func resetToDefault() async throws {
        try await store.save(BillBookData.default())
    }

    func clearData() async throws {
        try await store.deleteFile()
    }
}
