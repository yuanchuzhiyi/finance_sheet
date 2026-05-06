import Foundation

#if canImport(UIKit)
import UIKit

enum PDFGenerator {
    struct Config {
        var pageSize = CGSize(width: 595, height: 842) // A4 @ 72dpi
        var margin: CGFloat = 40
    }

    static func makeFileName(viewMode: ViewMode, period: String) -> String {
        let title = (viewMode == .day) ? "资产负债表" : "利润表"
        let compact = period.replacingOccurrences(of: "-", with: "")
        return "\(title)_\(compact).pdf"
    }

    static func generate(
        reportData: ReportData,
        viewMode: ViewMode,
        period: String,
        summary: Summary,
        to destinationURL: URL,
        config: Config = Config()
    ) throws {
        let renderer = UIGraphicsPDFRenderer(bounds: CGRect(origin: .zero, size: config.pageSize))

        if FileManager.default.fileExists(atPath: destinationURL.path) {
            try? FileManager.default.removeItem(at: destinationURL)
        }

        try renderer.writePDF(to: destinationURL) { context in
            context.beginPage()
            let cg = context.cgContext

            let pageWidth = config.pageSize.width
            let pageHeight = config.pageSize.height
            let margin = config.margin

            let numberFormatter: NumberFormatter = {
                let formatter = NumberFormatter()
                formatter.locale = Locale(identifier: "zh_CN")
                formatter.numberStyle = .decimal
                formatter.maximumFractionDigits = 2
                return formatter
            }()

            func format(_ value: Double) -> String {
                numberFormatter.string(from: NSNumber(value: value)) ?? "\(value)"
            }

            func drawText(
                _ text: String,
                x: CGFloat,
                y: CGFloat,
                font: UIFont,
                color: UIColor,
                alignment: NSTextAlignment = .left,
                width: CGFloat? = nil
            ) {
                let paragraph = NSMutableParagraphStyle()
                paragraph.alignment = alignment
                let attrs: [NSAttributedString.Key: Any] = [
                    .font: font,
                    .foregroundColor: color,
                    .paragraphStyle: paragraph,
                ]

                if let width {
                    let rect = CGRect(x: x, y: y, width: width, height: font.lineHeight * 1.4)
                    (text as NSString).draw(in: rect, withAttributes: attrs)
                } else {
                    (text as NSString).draw(at: CGPoint(x: x, y: y), withAttributes: attrs)
                }
            }

            func drawLine(y: CGFloat) {
                cg.setStrokeColor(UIColor(red: 0.886, green: 0.91, blue: 0.941, alpha: 1).cgColor) // #E2E8F0
                cg.setLineWidth(1)
                cg.move(to: CGPoint(x: margin, y: y))
                cg.addLine(to: CGPoint(x: pageWidth - margin, y: y))
                cg.strokePath()
            }

            var y = margin

            let title = (viewMode == .day) ? "资产负债表" : "利润表"
            drawText(title, x: margin, y: y, font: .boldSystemFont(ofSize: 24), color: UIColor(red: 0.118, green: 0.161, blue: 0.231, alpha: 1)) // #1E293B
            y += 35

            drawText("FAMILY FINANCE - \(period)", x: margin, y: y, font: .systemFont(ofSize: 12), color: UIColor(red: 0.392, green: 0.455, blue: 0.545, alpha: 1)) // #64748B
            y += 30

            let dtf = DateFormatter()
            dtf.locale = Locale(identifier: "zh_CN")
            dtf.dateFormat = "yyyy-MM-dd HH:mm"
            drawText("生成日期: \(dtf.string(from: Date()))", x: margin, y: y, font: .systemFont(ofSize: 12), color: UIColor(red: 0.392, green: 0.455, blue: 0.545, alpha: 1))
            y += 40

            // Summary background
            let summaryRect = CGRect(x: margin, y: y, width: pageWidth - 2 * margin, height: 80)
            cg.setFillColor(UIColor(red: 0.945, green: 0.961, blue: 0.976, alpha: 1).cgColor) // #F1F5F9
            cg.fill(summaryRect)
            y += 20

            let headerFont = UIFont.boldSystemFont(ofSize: 14)
            let valueFont = UIFont.systemFont(ofSize: 12)
            let headerColor = UIColor(red: 0.31, green: 0.275, blue: 0.898, alpha: 1) // #4F46E5

            if viewMode == .day {
                drawText("资产总额", x: margin + 10, y: y, font: headerFont, color: headerColor)
                drawText("¥ \(format(summary.asset))", x: margin + 150, y: y, font: valueFont, color: UIColor(red: 0.31, green: 0.275, blue: 0.898, alpha: 1), alignment: .right, width: 140)

                drawText("负债总额", x: margin + 200, y: y, font: headerFont, color: headerColor)
                drawText("¥ \(format(summary.liability))", x: margin + 350, y: y, font: valueFont, color: UIColor(red: 0.851, green: 0.467, blue: 0.024, alpha: 1), alignment: .right, width: 140) // #D97706

                y += 30

                drawText("所有者权益（净资产）", x: margin + 10, y: y, font: headerFont, color: headerColor)
                let netWorthColor: UIColor = (summary.netWorth >= 0)
                    ? UIColor(red: 0.02, green: 0.588, blue: 0.412, alpha: 1) // #059669
                    : UIColor(red: 0.882, green: 0.114, blue: 0.282, alpha: 1) // #E11D48
                drawText("¥ \(format(summary.netWorth))", x: margin + 250, y: y, font: valueFont, color: netWorthColor, alignment: .right, width: 240)
            } else {
                drawText("本期收入", x: margin + 10, y: y, font: headerFont, color: headerColor)
                drawText("¥ \(format(summary.income))", x: margin + 150, y: y, font: valueFont, color: UIColor(red: 0.02, green: 0.588, blue: 0.412, alpha: 1), alignment: .right, width: 140)

                drawText("本期支出", x: margin + 200, y: y, font: headerFont, color: headerColor)
                drawText("¥ \(format(summary.expense))", x: margin + 350, y: y, font: valueFont, color: UIColor(red: 0.882, green: 0.114, blue: 0.282, alpha: 1), alignment: .right, width: 140)

                y += 30

                drawText("本期利润（结余）", x: margin + 10, y: y, font: headerFont, color: headerColor)
                let profitColor: UIColor = (summary.cashflow >= 0)
                    ? UIColor(red: 0.02, green: 0.588, blue: 0.412, alpha: 1)
                    : UIColor(red: 0.882, green: 0.114, blue: 0.282, alpha: 1)
                drawText("¥ \(format(summary.cashflow))", x: margin + 250, y: y, font: valueFont, color: profitColor, alignment: .right, width: 240)
            }

            y = summaryRect.maxY + 10

            let groups = reportData.displayGroups(for: viewMode)
            for group in groups {
                if y > pageHeight - 100 { break }

                let groupColor: UIColor = {
                    switch group.type {
                    case .income: return UIColor(red: 0.02, green: 0.588, blue: 0.412, alpha: 1) // #059669
                    case .expense: return UIColor(red: 0.882, green: 0.114, blue: 0.282, alpha: 1) // #E11D48
                    case .asset: return UIColor(red: 0.31, green: 0.275, blue: 0.898, alpha: 1) // #4F46E5
                    case .liability: return UIColor(red: 0.851, green: 0.467, blue: 0.024, alpha: 1) // #D97706
                    }
                }()

                drawText(group.name, x: margin, y: y, font: UIFont.boldSystemFont(ofSize: 14), color: groupColor)
                y += 22
                drawLine(y: y)
                y += 6

                // Header
                drawText("科目", x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.392, green: 0.455, blue: 0.545, alpha: 1))
                drawText("金额", x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.392, green: 0.455, blue: 0.545, alpha: 1), alignment: .right, width: pageWidth - 2 * margin)
                y += 18

                for item in group.items {
                    let value = item.value(for: period)
                    drawText(item.name, x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.204, green: 0.271, blue: 0.333, alpha: 1)) // #334155
                    drawText("¥ \(format(value))", x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.118, green: 0.161, blue: 0.231, alpha: 1), alignment: .right, width: pageWidth - 2 * margin)
                    y += 18

                    if let children = item.children {
                        for child in children {
                            let childValue = child.value(for: period)
                            drawText("  └ \(child.name)", x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.204, green: 0.271, blue: 0.333, alpha: 1))
                            drawText("¥ \(format(childValue))", x: margin, y: y, font: UIFont.systemFont(ofSize: 12), color: UIColor(red: 0.118, green: 0.161, blue: 0.231, alpha: 1), alignment: .right, width: pageWidth - 2 * margin)
                            y += 18
                        }
                    }
                }

                drawLine(y: y)
                y += 6
                drawText("小计", x: margin, y: y, font: UIFont.boldSystemFont(ofSize: 13), color: UIColor(red: 0.118, green: 0.161, blue: 0.231, alpha: 1))
                drawText("¥ \(format(group.total(for: period)))", x: margin, y: y, font: UIFont.boldSystemFont(ofSize: 13), color: groupColor, alignment: .right, width: pageWidth - 2 * margin)
                y += 26
            }
        }
    }
}
#endif

