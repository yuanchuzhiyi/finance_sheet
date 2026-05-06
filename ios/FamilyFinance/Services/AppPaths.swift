import Foundation

enum AppPaths {
    static let appSupportDirectory: URL = {
        let fileManager = FileManager.default
        let base = fileManager.urls(for: .applicationSupportDirectory, in: .userDomainMask).first!
        let dir = base.appendingPathComponent("FamilyFinance", isDirectory: true)
        if !fileManager.fileExists(atPath: dir.path) {
            try? fileManager.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir
    }()

    static let documentsDirectory: URL = {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    }()

    static let reportDataURL: URL = appSupportDirectory.appendingPathComponent("report_data.json")
    static let billBookDataURL: URL = appSupportDirectory.appendingPathComponent("bill_book_data.json")
}

