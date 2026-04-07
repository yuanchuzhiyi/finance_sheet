package com.familyfinance.sheet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.sheet.data.local.DataStoreManager
import com.familyfinance.sheet.data.model.CategoryGroup
import com.familyfinance.sheet.data.model.MetricComparison
import com.familyfinance.sheet.data.model.NoteItem
import com.familyfinance.sheet.data.model.ReportData
import com.familyfinance.sheet.data.model.ReportItem
import com.familyfinance.sheet.data.model.Summary
import com.familyfinance.sheet.data.model.SummaryComparison
import com.familyfinance.sheet.data.model.ViewMode
import com.familyfinance.sheet.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 主页面 ViewModel
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dataStoreManager = DataStoreManager(application)
    private val repository = ReportRepository(dataStoreManager)
    
    // 视图模式
    private val _viewMode = MutableStateFlow(ViewMode.DAY)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()
    
    // 选中的周期
    private val _selectedYear = MutableStateFlow("")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()
    
    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()
    
    private val _selectedDay = MutableStateFlow("")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()
    
    // 对比周期
    private val _comparisonPeriod = MutableStateFlow("")
    val comparisonPeriod: StateFlow<String> = _comparisonPeriod.asStateFlow()
    
    // 是否显示备注
    private val _showNotes = MutableStateFlow(false)
    val showNotes: StateFlow<Boolean> = _showNotes.asStateFlow()
    
    // 报表数据
    val reportData: StateFlow<ReportData> = repository.reportDataFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReportData.default()
    )
    
    // 当前选中的周期
    val currentPeriod: StateFlow<String> = combine(
        viewMode, selectedYear, selectedMonth, selectedDay
    ) { mode, year, month, day ->
        when (mode) {
            ViewMode.YEAR -> year
            ViewMode.MONTH -> month
            ViewMode.DAY -> day
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    // 汇总数据
    val summary: StateFlow<Summary> = combine(
        reportData, viewMode, currentPeriod
    ) { data, mode, period ->
        data.getSummary(mode, period)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Summary())
    
    val comparisonOptions: StateFlow<List<String>> = combine(
        reportData, viewMode, currentPeriod
    ) { data, mode, period ->
        data.getPeriods(mode).filter { it != period }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val summaryComparison: StateFlow<SummaryComparison?> = combine(
        reportData, viewMode, currentPeriod, comparisonPeriod
    ) { data, mode, period, comparison ->
        data.getSummaryComparison(
            viewMode = mode,
            currentPeriod = period,
            comparisonPeriod = comparison
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 显示的分组
    val displayGroups: StateFlow<List<CategoryGroup>> = combine(
        reportData, viewMode
    ) { data, mode ->
        data.getDisplayGroups(mode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    init {
        // 初始化选中的周期
        viewModelScope.launch {
            reportData.collect { data ->
                if (_selectedYear.value.isEmpty() && data.years.isNotEmpty()) {
                    _selectedYear.value = data.years.maxOrNull().orEmpty()
                }
                if (_selectedMonth.value.isEmpty() && data.months.isNotEmpty()) {
                    _selectedMonth.value = data.months.maxOrNull().orEmpty()
                }
                if (_selectedDay.value.isEmpty() && data.days.isNotEmpty()) {
                    _selectedDay.value = data.days.maxOrNull().orEmpty()
                }
                
                // 如果数据中没有默认分组，自动创建并保存
                val displayGroups = data.getDisplayGroups(_viewMode.value)
                if (displayGroups.isEmpty()) {
                    val updatedData = data.ensureDefaultGroups()
                    if (updatedData != data) {
                        repository.saveReportData(updatedData)
                    }
                }
            }
        }
        
        viewModelScope.launch {
            combine(reportData, viewMode, currentPeriod) { data, mode, period ->
                data.getPeriods(mode).filter { it != period }
            }.collect { options ->
                if (_comparisonPeriod.value !in options) {
                    _comparisonPeriod.value = ""
                }
            }
        }
    }
    
    // 切换视图模式
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }
    
    // 切换备注显示
    fun toggleShowNotes() {
        _showNotes.value = !_showNotes.value
    }
    
    // 选择年份
    fun selectYear(year: String) {
        _selectedYear.value = year
    }
    
    // 选择月份
    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }
    
    // 选择日期
    fun selectDay(day: String) {
        _selectedDay.value = day
    }
    
    fun selectComparisonPeriod(period: String) {
        _comparisonPeriod.value = period
    }
    
    // 添加日期
    fun addDay(day: String): Boolean {
        val data = reportData.value
        if (!day.matches(Regex("\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])"))) {
            return false
        }
        if (data.days.contains(day)) {
            return false
        }
        viewModelScope.launch {
            val newData = data.addDay(day)
            repository.saveReportData(newData)
            _selectedDay.value = day
            _viewMode.value = ViewMode.DAY
        }
        return true
    }
    
    // 添加月份
    fun addMonth(month: String): Boolean {
        val data = reportData.value
        if (!month.matches(Regex("\\d{4}-(0[1-9]|1[0-2])"))) {
            return false
        }
        if (data.months.contains(month)) {
            return false
        }
        viewModelScope.launch {
            val newData = data.addMonth(month)
            repository.saveReportData(newData)
            _selectedMonth.value = month
            _viewMode.value = ViewMode.MONTH
        }
        return true
    }
    
    // 添加年份
    fun addYear(year: String): Boolean {
        val data = reportData.value
        if (!year.matches(Regex("\\d{4}"))) {
            return false
        }
        if (data.years.contains(year)) {
            return false
        }
        viewModelScope.launch {
            val newData = data.addYear(year)
            repository.saveReportData(newData)
            _selectedYear.value = year
            _viewMode.value = ViewMode.YEAR
        }
        return true
    }
    
    // 添加科目
    fun addItem(groupId: String, name: String) {
        viewModelScope.launch {
            var data = reportData.value
            // 确保数据中有默认分组，这样 updateGroup 才能找到它们
            data = data.ensureDefaultGroups()
            
            val allPeriods = data.years + data.months + data.days
            val newItem = ReportItem(
                id = "${groupId}_${System.currentTimeMillis()}",
                name = name,
                values = allPeriods.associateWith { 0.0 }
            )
            val newData = data.updateGroup(groupId) { group ->
                group.addItem(newItem)
            }
            repository.saveReportData(newData)
        }
    }
    
    // 添加子科目
    fun addSubItem(groupId: String, parentId: String, name: String) {
        viewModelScope.launch {
            val data = reportData.value
            val allPeriods = data.years + data.months + data.days
            val newItem = ReportItem(
                id = "${parentId}_${System.currentTimeMillis()}",
                name = name,
                values = allPeriods.associateWith { 0.0 }
            )
            val newData = data.updateGroup(groupId) { group ->
                group.addSubItem(parentId, newItem)
            }
            repository.saveReportData(newData)
        }
    }
    
    // 重命名科目
    fun renameItem(groupId: String, itemId: String, newName: String) {
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.updateGroup(groupId) { group ->
                group.updateItem(itemId) { item ->
                    item.rename(newName)
                }
            }
            repository.saveReportData(newData)
        }
    }
    
    // 删除科目
    fun deleteItem(groupId: String, itemId: String) {
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.updateGroup(groupId) { group ->
                group.removeItem(itemId)
            }
            repository.saveReportData(newData)
        }
    }
    
    // 更新科目值
    fun updateItemValue(groupId: String, itemId: String, period: String, value: Double) {
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.updateGroup(groupId) { group ->
                group.updateItem(itemId) { item ->
                    item.updateValue(period, value)
                }
            }
            repository.saveReportData(newData)
        }
    }
    
    // 更新科目备注
    fun updateItemNote(groupId: String, itemId: String, period: String, note: String) {
        if (period.isBlank()) return
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.updateGroup(groupId) { group ->
                group.updateItem(itemId) { item ->
                    item.updateNote(period, note)
                }
            }
            repository.saveReportData(newData)
        }
    }
    
    // 添加附注
    fun addNote(period: String) {
        if (period.isBlank()) return
        viewModelScope.launch {
            val data = reportData.value
            val newNote = NoteItem(
                id = "note_${System.currentTimeMillis()}",
                label = "",
                value = ""
            )
            val newData = data.addNote(period, newNote)
            repository.saveReportData(newData)
        }
    }
    
    // 更新附注
    fun updateNote(period: String, noteId: String, label: String, value: String) {
        if (period.isBlank()) return
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.updateNote(period, noteId) { note ->
                note.copy(label = label, value = value)
            }
            repository.saveReportData(newData)
        }
    }
    
    // 删除附注
    fun deleteNote(period: String, noteId: String) {
        if (period.isBlank()) return
        viewModelScope.launch {
            val data = reportData.value
            val newData = data.removeNote(period, noteId)
            repository.saveReportData(newData)
        }
    }
    
    // 重置报表
    fun resetReport() {
        viewModelScope.launch {
            repository.resetToDefault()
            _selectedYear.value = ""
            _selectedMonth.value = ""
            _selectedDay.value = ""
            _comparisonPeriod.value = ""
        }
    }
    
    // 删除报表
    fun deleteReport() {
        viewModelScope.launch {
            repository.deleteReport()
            _selectedYear.value = ""
            _selectedMonth.value = ""
            _selectedDay.value = ""
            _comparisonPeriod.value = ""
        }
    }
}
