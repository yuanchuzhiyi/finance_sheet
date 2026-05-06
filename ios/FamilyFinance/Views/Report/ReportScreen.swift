import SwiftUI

struct ReportScreen: View {
    @EnvironmentObject private var viewModel: ReportViewModel

    #if canImport(UIKit)
    @State private var textPrompt: TextFieldPrompt.Prompt?
    @State private var isGeneratingPDF = false
    @State private var pdfShareItem: IdentifiableURL?
    @State private var pdfPreviewItem: IdentifiableURL?
    #endif

    @State private var messageAlert: MessageAlert?
    @State private var confirmReset = false
    @State private var confirmDeleteReport = false
    @State private var deleteItemPrompt: DeleteItemPrompt?

    private var screenTitle: String {
        viewModel.viewMode == .day ? "资产负债表" : "利润表"
    }

    private var screenDesc: String {
        viewModel.viewMode == .day
            ? "按日记录资产/负债快照，汇总净资产。"
            : "按年/月查看收入、支出与结余（利润）。"
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(screenDesc)
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                actionsRow

                summarySection

                viewModeAndPeriodSection

                ForEach(viewModel.displayGroups) { group in
                    CategoryGroupCard(
                        group: group,
                        period: viewModel.currentPeriod,
                        showNotes: viewModel.showNotes,
                        onAddItem: { promptAddItem(groupId: group.id) },
                        onValueChange: { itemId, value in
                            viewModel.updateItemValue(groupId: group.id, itemId: itemId, period: viewModel.currentPeriod, value: value)
                        },
                        onNoteChange: { itemId, note in
                            viewModel.updateItemNote(groupId: group.id, itemId: itemId, note: note)
                        },
                        onRenameItem: { itemId in
                            guard let item = findItem(in: group.items, id: itemId) else { return }
                            promptRenameItem(groupId: group.id, itemId: itemId, currentName: item.name)
                        },
                        onAddSubItem: { parentId in
                            promptAddSubItem(groupId: group.id, parentId: parentId)
                        },
                        onDeleteItem: { itemId in
                            guard let item = findItem(in: group.items, id: itemId) else { return }
                            deleteItemPrompt = DeleteItemPrompt(groupId: group.id, itemId: itemId, itemName: item.name)
                        }
                    )
                }

                NotesSectionView(
                    notes: viewModel.reportData.notes,
                    onAddNote: { viewModel.addNote() },
                    onUpdateNote: { viewModel.updateNote(noteId: $0, label: $1, value: $2) },
                    onDeleteNote: { viewModel.deleteNote(noteId: $0) }
                )
            }
            .padding(16)
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                VStack(spacing: 2) {
                    Text("FAMILY FINANCE")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                        .tracking(2)
                    Text(screenTitle)
                        .font(.headline.weight(.semibold))
                }
            }
        }
        .alert(item: $messageAlert) { alert in
            Alert(title: Text(alert.title), message: Text(alert.message), dismissButton: .default(Text("确定")))
        }
        .alert("重置模板", isPresented: $confirmReset) {
            Button("重置", role: .destructive) { viewModel.resetReport() }
            Button("取消", role: .cancel) {}
        } message: {
            Text("确定要重置为默认模板吗？当前数据将被覆盖。")
        }
        .alert("删除报表", isPresented: $confirmDeleteReport) {
            Button("删除", role: .destructive) { viewModel.deleteReport() }
            Button("取消", role: .cancel) {}
        } message: {
            Text("确定要删除报表吗？（等价于重置为默认模板）")
        }
        .alert("删除科目", isPresented: Binding(get: { deleteItemPrompt != nil }, set: { if !$0 { deleteItemPrompt = nil } })) {
            Button("删除", role: .destructive) {
                guard let prompt = deleteItemPrompt else { return }
                viewModel.deleteItem(groupId: prompt.groupId, itemId: prompt.itemId)
                deleteItemPrompt = nil
            }
            Button("取消", role: .cancel) { deleteItemPrompt = nil }
        } message: {
            Text("确定删除科目“\(deleteItemPrompt?.itemName ?? "")”吗？")
        }
        #if canImport(UIKit)
        .textFieldPrompt($textPrompt)
        .sheet(item: $pdfShareItem) { item in
            ShareSheet(items: [item.url])
        }
        #if canImport(QuickLook)
        .sheet(item: $pdfPreviewItem) { item in
            QuickLookPreview(url: item.url)
        }
        #endif
        #endif
    }

    private var actionsRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                Button {
                    promptAddDay()
                } label: {
                    Label("新增日期", systemImage: "calendar.badge.plus")
                }
                .buttonStyle(.bordered)

                Button {
                    promptAddMonth()
                } label: {
                    Label("新增月份", systemImage: "calendar")
                }
                .buttonStyle(.bordered)

                Button {
                    promptAddYear()
                } label: {
                    Label("新增年份", systemImage: "calendar.circle")
                }
                .buttonStyle(.bordered)

                Button {
                    confirmReset = true
                } label: {
                    Label("重置模板", systemImage: "arrow.clockwise")
                }
                .buttonStyle(.bordered)

                Button {
                    viewModel.saveReport()
                    messageAlert = MessageAlert(title: "已保存", message: "报表已保存到本地。")
                } label: {
                    Label("保存", systemImage: "tray.and.arrow.down")
                }
                .buttonStyle(.bordered)

                Button {
                    previewPDF()
                } label: {
                    Label(isGeneratingPDF ? "生成中…" : "预览PDF", systemImage: "doc.richtext")
                }
                .buttonStyle(.bordered)
                .disabled(isGeneratingPDF)

                Button {
                    exportPDF()
                } label: {
                    Label(isGeneratingPDF ? "生成中…" : "导出PDF", systemImage: "square.and.arrow.down")
                }
                .buttonStyle(.bordered)
                .disabled(isGeneratingPDF)

                Button {
                    sharePDF()
                } label: {
                    Label(isGeneratingPDF ? "生成中…" : "分享PDF", systemImage: "square.and.arrow.up")
                }
                .buttonStyle(.bordered)
                .disabled(isGeneratingPDF)

                Button(role: .destructive) {
                    confirmDeleteReport = true
                } label: {
                    Label("删除报表", systemImage: "trash")
                }
                .buttonStyle(.bordered)
            }
        }
    }

    private var summarySection: some View {
        VStack(spacing: 12) {
            if viewModel.viewMode == .day {
                HStack(spacing: 12) {
                    SummaryCardView(
                        title: "资产总额",
                        value: viewModel.summary.asset,
                        accentColor: Theme.Colors.asset,
                        systemImage: "sum"
                    )
                    SummaryCardView(
                        title: "负债总额",
                        value: viewModel.summary.liability,
                        accentColor: Theme.Colors.liability
                    )
                }

                SummaryCardView(
                    title: "净资产",
                    value: viewModel.summary.netWorth,
                    accentColor: Theme.Colors.positiveNegative(viewModel.summary.netWorth)
                )
            } else {
                HStack(spacing: 12) {
                    SummaryCardView(
                        title: "本期收入",
                        value: viewModel.summary.income,
                        accentColor: Theme.Colors.income,
                        systemImage: "sum"
                    )
                    SummaryCardView(
                        title: "本期支出",
                        value: viewModel.summary.expense,
                        accentColor: Theme.Colors.expense
                    )
                }

                SummaryCardView(
                    title: "本期结余（利润）",
                    value: viewModel.summary.cashflow,
                    accentColor: Theme.Colors.positiveNegative(viewModel.summary.cashflow)
                )
            }
        }
    }

    private var viewModeAndPeriodSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("查看维度")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            ViewModePicker(selection: $viewModel.viewMode)

            HStack(spacing: 12) {
                switch viewModel.viewMode {
                case .year:
                    PeriodMenu(periods: viewModel.reportData.years, selection: $viewModel.selectedYear)
                case .month:
                    PeriodMenu(periods: viewModel.reportData.months, selection: $viewModel.selectedMonth)
                case .day:
                    PeriodMenu(periods: viewModel.reportData.days, selection: $viewModel.selectedDay)
                }

                Button {
                    viewModel.toggleShowNotes()
                } label: {
                    Text(viewModel.showNotes ? "折叠备注" : "展开备注")
                }
                .buttonStyle(.bordered)

                Spacer(minLength: 0)
            }
        }
    }

    private func promptAddDay() {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "新增日期",
            message: "格式：yyyy-MM-dd",
            placeholder: "例如：2025-01-01",
            keyboardType: .numbersAndPunctuation,
            onSubmit: { day in
                if !viewModel.addDay(day) {
                    messageAlert = MessageAlert(title: "失败", message: "日期格式错误或已存在。")
                }
            }
        )
        #else
        messageAlert = MessageAlert(title: "提示", message: "当前平台不支持输入弹窗。")
        #endif
    }

    private func promptAddMonth() {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "新增月份",
            message: "格式：yyyy-MM",
            placeholder: "例如：2025-02",
            keyboardType: .numbersAndPunctuation,
            onSubmit: { month in
                if !viewModel.addMonth(month) {
                    messageAlert = MessageAlert(title: "失败", message: "月份格式错误或已存在。")
                }
            }
        )
        #else
        messageAlert = MessageAlert(title: "提示", message: "当前平台不支持输入弹窗。")
        #endif
    }

    private func promptAddYear() {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "新增年份",
            message: "格式：yyyy",
            placeholder: "例如：2026",
            keyboardType: .numberPad,
            onSubmit: { year in
                if !viewModel.addYear(year) {
                    messageAlert = MessageAlert(title: "失败", message: "年份格式错误或已存在。")
                }
            }
        )
        #else
        messageAlert = MessageAlert(title: "提示", message: "当前平台不支持输入弹窗。")
        #endif
    }

    private func promptAddItem(groupId: String) {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "新增科目",
            placeholder: "科目名称",
            onSubmit: { name in
                viewModel.addItem(groupId: groupId, name: name)
            }
        )
        #endif
    }

    private func promptAddSubItem(groupId: String, parentId: String) {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "新增子科目",
            placeholder: "子科目名称",
            onSubmit: { name in
                viewModel.addSubItem(groupId: groupId, parentId: parentId, name: name)
            }
        )
        #endif
    }

    private func promptRenameItem(groupId: String, itemId: String, currentName: String) {
        #if canImport(UIKit)
        textPrompt = .init(
            title: "重命名科目",
            placeholder: "新名称",
            initialText: currentName,
            onSubmit: { name in
                viewModel.renameItem(groupId: groupId, itemId: itemId, newName: name)
            }
        )
        #endif
    }

    private func findItem(in items: [ReportItem], id: String) -> ReportItem? {
        for item in items {
            if item.id == id { return item }
            if let children = item.children, let match = findItem(in: children, id: id) {
                return match
            }
        }
        return nil
    }

    private func previewPDF() {
        #if canImport(UIKit)
        guard !viewModel.currentPeriod.isEmpty else {
            messageAlert = MessageAlert(title: "无法预览", message: "请先选择周期。")
            return
        }

        let fileName = PDFGenerator.makeFileName(viewMode: viewModel.viewMode, period: viewModel.currentPeriod)
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        generatePDF(to: url) { generatedURL in
            pdfPreviewItem = IdentifiableURL(url: generatedURL)
        }
        #else
        messageAlert = MessageAlert(title: "不支持", message: "当前平台不支持 PDF 预览。")
        #endif
    }

    private func exportPDF() {
        #if canImport(UIKit)
        guard !viewModel.currentPeriod.isEmpty else {
            messageAlert = MessageAlert(title: "无法导出", message: "请先选择周期。")
            return
        }

        let fileName = PDFGenerator.makeFileName(viewMode: viewModel.viewMode, period: viewModel.currentPeriod)
        let url = AppPaths.documentsDirectory.appendingPathComponent(fileName)
        generatePDF(to: url) { generatedURL in
            messageAlert = MessageAlert(title: "已导出", message: "PDF 已保存到 Documents：\(generatedURL.lastPathComponent)")
        }
        #else
        messageAlert = MessageAlert(title: "不支持", message: "当前平台不支持 PDF 导出。")
        #endif
    }

    private func sharePDF() {
        #if canImport(UIKit)
        guard !viewModel.currentPeriod.isEmpty else {
            messageAlert = MessageAlert(title: "无法分享", message: "请先选择周期。")
            return
        }

        let fileName = PDFGenerator.makeFileName(viewMode: viewModel.viewMode, period: viewModel.currentPeriod)
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)
        generatePDF(to: url) { generatedURL in
            pdfShareItem = IdentifiableURL(url: generatedURL)
        }
        #else
        messageAlert = MessageAlert(title: "不支持", message: "当前平台不支持分享。")
        #endif
    }

    private func generatePDF(to destination: URL, onSuccess: @escaping (URL) -> Void) {
        #if canImport(UIKit)
        let reportData = viewModel.reportData
        let viewMode = viewModel.viewMode
        let period = viewModel.currentPeriod
        let summary = viewModel.summary

        isGeneratingPDF = true
        Task {
            let result = await Task.detached(priority: .userInitiated) { () -> Result<URL, Error> in
                do {
                    try PDFGenerator.generate(
                        reportData: reportData,
                        viewMode: viewMode,
                        period: period,
                        summary: summary,
                        to: destination
                    )
                    return .success(destination)
                } catch {
                    return .failure(error)
                }
            }.value

            isGeneratingPDF = false
            switch result {
            case .success(let url):
                onSuccess(url)
            case .failure:
                messageAlert = MessageAlert(title: "失败", message: "PDF 生成失败。")
            }
        }
        #endif
    }
}

private struct DeleteItemPrompt {
    let groupId: String
    let itemId: String
    let itemName: String
}

private struct MessageAlert: Identifiable {
    let id = UUID()
    let title: String
    let message: String
}
