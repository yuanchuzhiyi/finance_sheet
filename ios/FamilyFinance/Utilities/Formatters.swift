import Foundation

enum Formatters {
    static let zhNumber: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.locale = Locale(identifier: "zh_CN")
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    static func formatNumber(_ value: Double) -> String {
        zhNumber.string(from: NSNumber(value: value)) ?? "\(value)"
    }
}

