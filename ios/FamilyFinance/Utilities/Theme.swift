import SwiftUI

enum Theme {
    enum Colors {
        static let income = Color.green
        static let expense = Color.red
        static let asset = Color.indigo
        static let liability = Color.orange

        static func positiveNegative(_ value: Double) -> Color {
            value >= 0 ? income : expense
        }
    }
}

