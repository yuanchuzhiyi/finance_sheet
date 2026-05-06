import SwiftUI

extension Color {
    /// `argb` example: `0xFF9E9E9E`
    init(argb: UInt64) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self = Color(.sRGB, red: r, green: g, blue: b, opacity: a)
    }
}

