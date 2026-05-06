import Foundation

enum DateUtils {
    static let isoDayFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = .current
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    static func isoDayString(from date: Date) -> String {
        isoDayFormatter.string(from: date)
    }

    static func date(fromISODateString value: String) -> Date? {
        isoDayFormatter.date(from: value)
    }

    static func nowMillis() -> Int64 {
        Int64(Date().timeIntervalSince1970 * 1000)
    }
}
