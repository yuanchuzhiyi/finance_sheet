import Foundation

@MainActor
final class ReportViewModel: ObservableObject {
    @Published private(set) var reportData: ReportData = .default()

    @Published var viewMode: ViewMode = .day
    @Published var selectedYear: String = ""
    @Published var selectedMonth: String = ""
    @Published var selectedDay: String = ""
    @Published var showNotes: Bool = false

    private let repository: ReportRepository

    init(repository: ReportRepository = ReportRepository()) {
        self.repository = repository
        Task { await load() }
    }

    var currentPeriod: String {
        switch viewMode {
        case .year: return selectedYear
        case .month: return selectedMonth
        case .day: return selectedDay
        }
    }

    var summary: Summary {
        reportData.summary(for: viewMode, period: currentPeriod)
    }

    var displayGroups: [CategoryGroup] {
        reportData.displayGroups(for: viewMode)
    }

    func load() async {
        let data = await repository.loadReportData()
        reportData = data
        ensureSelectedPeriods()
    }

    func saveReport() {
        Task {
            try? await repository.saveReportData(reportData)
        }
    }

    func resetReport() {
        reportData = .default()
        ensureSelectedPeriods()
        saveReport()
    }

    func deleteReport() {
        resetReport()
    }

    func setViewMode(_ mode: ViewMode) {
        viewMode = mode
    }

    func toggleShowNotes() {
        showNotes.toggle()
    }

    func selectYear(_ year: String) {
        selectedYear = year
    }

    func selectMonth(_ month: String) {
        selectedMonth = month
    }

    func selectDay(_ day: String) {
        selectedDay = day
    }

    func addDay(_ day: String) -> Bool {
        let data = reportData
        guard day.matches(regex: #"^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$"#) else { return false }
        guard !data.days.contains(day) else { return false }

        reportData = data.addingDay(day)
        selectedDay = day
        viewMode = .day
        saveReport()
        return true
    }

    func addMonth(_ month: String) -> Bool {
        let data = reportData
        guard month.matches(regex: #"^\d{4}-(0[1-9]|1[0-2])$"#) else { return false }
        guard !data.months.contains(month) else { return false }

        reportData = data.addingMonth(month)
        selectedMonth = month
        viewMode = .month
        saveReport()
        return true
    }

    func addYear(_ year: String) -> Bool {
        let data = reportData
        guard year.matches(regex: #"^\d{4}$"#) else { return false }
        guard !data.years.contains(year) else { return false }

        reportData = data.addingYear(year)
        selectedYear = year
        viewMode = .year
        saveReport()
        return true
    }

    func addItem(groupId: String, name: String) {
        var data = reportData
        let allPeriods = data.years + data.months + data.days
        let item = ReportItem(
            id: "\(groupId)_\(DateUtils.nowMillis())",
            name: name,
            values: Dictionary(uniqueKeysWithValues: allPeriods.map { ($0, 0.0) })
        )
        data = data.updatingGroup(groupId) { $0.addingItem(item) }
        reportData = data
        saveReport()
    }

    func addSubItem(groupId: String, parentId: String, name: String) {
        var data = reportData
        let allPeriods = data.years + data.months + data.days
        let item = ReportItem(
            id: "\(parentId)_\(DateUtils.nowMillis())",
            name: name,
            values: Dictionary(uniqueKeysWithValues: allPeriods.map { ($0, 0.0) })
        )
        data = data.updatingGroup(groupId) { $0.addingSubItem(parentId: parentId, child: item) }
        reportData = data
        saveReport()
    }

    func renameItem(groupId: String, itemId: String, newName: String) {
        reportData = reportData.updatingGroup(groupId) { group in
            group.updatingItem(itemId) { $0.renamed(newName) }
        }
        saveReport()
    }

    func deleteItem(groupId: String, itemId: String) {
        reportData = reportData.updatingGroup(groupId) { $0.removingItem(itemId) }
        saveReport()
    }

    func updateItemValue(groupId: String, itemId: String, period: String, value: Double) {
        reportData = reportData.updatingGroup(groupId) { group in
            group.updatingItem(itemId) { $0.updatingValue(for: period, to: value) }
        }
        saveReport()
    }

    func updateItemNote(groupId: String, itemId: String, note: String) {
        reportData = reportData.updatingGroup(groupId) { group in
            group.updatingItem(itemId) { $0.updatingNote(note) }
        }
        saveReport()
    }

    func addNote() {
        let note = NoteItem(id: "note_\(DateUtils.nowMillis())", label: "", value: "")
        reportData = reportData.addingNote(note)
        saveReport()
    }

    func updateNote(noteId: String, label: String, value: String) {
        reportData = reportData.updatingNote(noteId) { note in
            var next = note
            next.label = label
            next.value = value
            return next
        }
        saveReport()
    }

    func deleteNote(noteId: String) {
        reportData = reportData.removingNote(noteId)
        saveReport()
    }

    private func ensureSelectedPeriods() {
        if selectedYear.isEmpty, let last = reportData.years.last {
            selectedYear = last
        }
        if selectedMonth.isEmpty, let last = reportData.months.last {
            selectedMonth = last
        }
        if selectedDay.isEmpty, let last = reportData.days.last {
            selectedDay = last
        }
    }
}

