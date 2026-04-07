package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

/**
 * 附注项
 */
@Serializable
data class NoteItem(
    val id: String,
    val label: String,
    val value: String
)

/**
 * 报表数据
 * @property years 年份列表
 * @property months 月份列表
 * @property days 日期列表
 * @property groups 综合分组（兼容旧逻辑）
 * @property flowGroups 按年/月统计的收支变动（income/expense）
 * @property balanceGroups 按日统计的资产状况（asset/liability）
 * @property notesByPeriod 按周期存储的附注（不参与汇总）
 * @property notes 旧版共享附注，保留用于兼容历史数据
 */
@Serializable
data class ReportData(
    val years: List<String> = emptyList(),
    val months: List<String> = emptyList(),
    val days: List<String> = emptyList(),
    val groups: List<CategoryGroup> = emptyList(),
    val flowGroups: List<CategoryGroup> = emptyList(),
    val balanceGroups: List<CategoryGroup> = emptyList(),
    val notesByPeriod: Map<String, List<NoteItem>> = emptyMap(),
    val notes: List<NoteItem> = emptyList()
) {
    companion object {
        /**
         * 创建默认报表数据（包含默认分组，但不包含任何日期和值）
         */
        fun default(): ReportData {
            val defaultPeriods = emptyList<String>()
            val defaultMonths = emptyList<String>()
            val defaultDays = emptyList<String>()
            
            // 创建默认的资产负债表分组（资产和负债）
            val defaultBalanceGroups = listOf(
                CategoryGroup(
                    id = "asset",
                    type = CategoryType.ASSET,
                    name = "资产 (Assets)",
                    items = emptyList()
                ),
                CategoryGroup(
                    id = "liability",
                    type = CategoryType.LIABILITY,
                    name = "负债 (Liabilities)",
                    items = emptyList()
                )
            )
            
            // 创建默认的利润表分组（收入和支出）
            val defaultFlowGroups = listOf(
                CategoryGroup(
                    id = "income",
                    type = CategoryType.INCOME,
                    name = "收入 (Income)",
                    items = emptyList()
                ),
                CategoryGroup(
                    id = "expense",
                    type = CategoryType.EXPENSE,
                    name = "支出 (Expenses)",
                    items = emptyList()
                )
            )
            
            // 合并所有分组
            val allGroups = defaultBalanceGroups + defaultFlowGroups
            
            return ReportData(
                years = defaultPeriods,
                months = defaultMonths,
                days = defaultDays,
                groups = allGroups,
                flowGroups = defaultFlowGroups,
                balanceGroups = defaultBalanceGroups,
                notesByPeriod = emptyMap(),
                notes = emptyList()
            )
        }
    }
    
    /**
     * 获取汇总数据
     */
    fun getSummary(viewMode: ViewMode, selectedPeriod: String): Summary {
        val targetGroups = when (viewMode) {
            ViewMode.DAY -> balanceGroups.ifEmpty {
                groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
            }
            else -> flowGroups.ifEmpty {
                groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
            }
        }
        
        var income = 0.0
        var expense = 0.0
        var asset = 0.0
        var liability = 0.0
        
        targetGroups.forEach { group ->
            val total = group.getTotal(selectedPeriod)
            when (group.type) {
                CategoryType.INCOME -> income += total
                CategoryType.EXPENSE -> expense += total
                CategoryType.ASSET -> asset += total
                CategoryType.LIABILITY -> liability += total
            }
        }
        
        return Summary(
            income = income,
            expense = expense,
            asset = asset,
            liability = liability,
            cashflow = income - expense,
            netWorth = asset - liability
        )
    }
    
    /**
     * 获取显示的分组
     * 确保返回的分组都在实际数据中，以便更新操作能正常工作
     */
    fun getDisplayGroups(viewMode: ViewMode): List<CategoryGroup> {
        val result = when (viewMode) {
            ViewMode.DAY -> balanceGroups.ifEmpty {
                groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
            }
            else -> flowGroups.ifEmpty {
                groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
            }
        }
        
        // 如果结果为空，返回空列表（不再创建新对象）
        // 实际数据会在 ensureDefaultGroups() 中创建
        return result
    }
    
    /**
     * 获取当前视图可用的周期选项
     */
    fun getPeriods(viewMode: ViewMode): List<String> {
        return when (viewMode) {
            ViewMode.YEAR -> years
            ViewMode.MONTH -> months
            ViewMode.DAY -> days
        }.sortedDescending()
    }
    
    /**
     * 确保数据中包含必要的默认分组
     * 如果 balanceGroups 或 flowGroups 为空，添加默认分组
     */
    fun ensureDefaultGroups(): ReportData {
        var updatedBalanceGroups = balanceGroups
        var updatedFlowGroups = flowGroups
        var updatedGroups = groups.toMutableList()
        
        // 确保有资产和负债分组
        if (balanceGroups.isEmpty()) {
            val assetGroup = CategoryGroup(
                id = "asset",
                type = CategoryType.ASSET,
                name = "资产 (Assets)",
                items = emptyList()
            )
            val liabilityGroup = CategoryGroup(
                id = "liability",
                type = CategoryType.LIABILITY,
                name = "负债 (Liabilities)",
                items = emptyList()
            )
            updatedBalanceGroups = listOf(assetGroup, liabilityGroup)
            
            // 如果 groups 中也没有这些分组，添加到 groups
            if (!groups.any { it.id == "asset" }) {
                updatedGroups.add(assetGroup)
            }
            if (!groups.any { it.id == "liability" }) {
                updatedGroups.add(liabilityGroup)
            }
        }
        
        // 确保有收入和支出分组
        if (flowGroups.isEmpty()) {
            val incomeGroup = CategoryGroup(
                id = "income",
                type = CategoryType.INCOME,
                name = "收入 (Income)",
                items = emptyList()
            )
            val expenseGroup = CategoryGroup(
                id = "expense",
                type = CategoryType.EXPENSE,
                name = "支出 (Expenses)",
                items = emptyList()
            )
            updatedFlowGroups = listOf(incomeGroup, expenseGroup)
            
            // 如果 groups 中也没有这些分组，添加到 groups
            if (!groups.any { it.id == "income" }) {
                updatedGroups.add(incomeGroup)
            }
            if (!groups.any { it.id == "expense" }) {
                updatedGroups.add(expenseGroup)
            }
        }
        
        return if (updatedBalanceGroups != balanceGroups || 
                   updatedFlowGroups != flowGroups || 
                   updatedGroups.size != groups.size) {
            copy(
                balanceGroups = updatedBalanceGroups,
                flowGroups = updatedFlowGroups,
                groups = updatedGroups
            )
        } else {
            this
        }
    }
    
    /**
     * 更新分组
     */
    fun updateGroup(groupId: String, updater: (CategoryGroup) -> CategoryGroup): ReportData {
        val updateList: (List<CategoryGroup>) -> List<CategoryGroup> = { list ->
            list.map { if (it.id == groupId) updater(it) else it }
        }
        return copy(
            flowGroups = updateList(flowGroups),
            balanceGroups = updateList(balanceGroups),
            groups = updateList(groups)
        )
    }
    
    /**
     * 添加新日期
     */
    fun addDay(day: String): ReportData {
        if (days.contains(day)) return this
        return copy(days = (days + day).sortedDescending())
    }
    
    /**
     * 添加新月份
     */
    fun addMonth(month: String): ReportData {
        if (months.contains(month)) return this
        return copy(months = (months + month).sortedDescending())
    }
    
    /**
     * 添加新年份
     */
    fun addYear(year: String): ReportData {
        if (years.contains(year)) return this
        return copy(years = (years + year).sortedDescending())
    }
    
    /**
     * 添加附注
     */
    fun getNotes(period: String): List<NoteItem> {
        return if (notesByPeriod.containsKey(period)) {
            notesByPeriod[period].orEmpty()
        } else {
            notes
        }
    }
    
    /**
     * 添加附注
     */
    fun addNote(period: String, note: NoteItem): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            val currentNotes = getNotes(period)
            this[period] = currentNotes + note
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }
    
    /**
     * 更新附注
     */
    fun updateNote(period: String, noteId: String, updater: (NoteItem) -> NoteItem): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            val currentNotes = getNotes(period)
            this[period] = currentNotes.map { if (it.id == noteId) updater(it) else it }
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }
    
    /**
     * 删除附注
     */
    fun removeNote(period: String, noteId: String): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            val currentNotes = getNotes(period)
            this[period] = currentNotes.filter { it.id != noteId }
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }
    
    /**
     * 获取汇总对比数据
     */
    fun getSummaryComparison(
        viewMode: ViewMode,
        currentPeriod: String,
        comparisonPeriod: String
    ): SummaryComparison? {
        if (currentPeriod.isBlank() || comparisonPeriod.isBlank()) return null
        val currentSummary = getSummary(viewMode, currentPeriod)
        val comparisonSummary = getSummary(viewMode, comparisonPeriod)
        return SummaryComparison(
            income = compareMetric(currentSummary.income, comparisonSummary.income),
            expense = compareMetric(currentSummary.expense, comparisonSummary.expense),
            asset = compareMetric(currentSummary.asset, comparisonSummary.asset),
            liability = compareMetric(currentSummary.liability, comparisonSummary.liability),
            cashflow = compareMetric(currentSummary.cashflow, comparisonSummary.cashflow),
            netWorth = compareMetric(currentSummary.netWorth, comparisonSummary.netWorth)
        )
    }
}

/**
 * 汇总数据
 */
data class Summary(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val asset: Double = 0.0,
    val liability: Double = 0.0,
    val cashflow: Double = 0.0,
    val netWorth: Double = 0.0
)

data class MetricComparison(
    val currentValue: Double,
    val comparisonValue: Double,
    val delta: Double,
    val percentChange: Double?
)

data class SummaryComparison(
    val income: MetricComparison,
    val expense: MetricComparison,
    val asset: MetricComparison,
    val liability: MetricComparison,
    val cashflow: MetricComparison,
    val netWorth: MetricComparison
)

fun compareMetric(currentValue: Double, comparisonValue: Double): MetricComparison {
    val delta = currentValue - comparisonValue
    val percentChange = when {
        comparisonValue == 0.0 && currentValue == 0.0 -> 0.0
        comparisonValue == 0.0 -> null
        else -> delta / comparisonValue
    }
    return MetricComparison(
        currentValue = currentValue,
        comparisonValue = comparisonValue,
        delta = delta,
        percentChange = percentChange
    )
}

/**
 * 视图模式
 */
enum class ViewMode {
    YEAR,
    MONTH,
    DAY
}
