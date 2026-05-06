import Foundation

extension String {
    func matches(regex pattern: String) -> Bool {
        range(of: pattern, options: [.regularExpression]) != nil
    }
}

