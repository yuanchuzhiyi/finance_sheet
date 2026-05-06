package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteItem(
    val id: String,
    val label: String,
    val value: String
)

/**
 * 报表数据
 *
 * 类目按期独立：每个期（年/月/日）拥有自己的 `CategoryGroup` 快照，互不影响。
 *
 * @property flowGroupsByPeriod    年/月期 → 收支类目快照
 * @property balanceGroupsByPeriod 日期    → 资产负债类目快照
 * @property groups / flowGroups / balanceGroups 旧字段，仅作历史数据迁移源 + 无任何期时的默认模板，不再用于编辑。
 */
@Serializable
data class ReportData(
    val years: List<String> = emptyList(),
    val months: List<String> = emptyList(),
    val days: List<String> = emptyList(),
    val flowGroupsByPeriod: Map<String, List<CategoryGroup>> = emptyMap(),
    val balanceGroupsByPeriod: Map<String, List<CategoryGroup>> = emptyMap(),
    val groups: List<CategoryGroup> = emptyList(),
    val flowGroups: List<CategoryGroup> = emptyList(),
    val balanceGroups: List<CategoryGroup> = emptyList(),
    val notesByPeriod: Map<String, List<NoteItem>> = emptyMap(),
    val notes: List<NoteItem> = emptyList()
) {
    companion object {
        fun default(): ReportData = ReportData()

        private fun defaultBalanceTemplate() = listOf(
            CategoryGroup(id = "asset", type = CategoryType.ASSET, name = "资产 (Assets)"),
            CategoryGroup(id = "liability", type = CategoryType.LIABILITY, name = "负债 (Liabilities)")
        )

        private fun defaultFlowTemplate() = listOf(
            CategoryGroup(id = "income", type = CategoryType.INCOME, name = "收入 (Income)"),
            CategoryGroup(id = "expense", type = CategoryType.EXPENSE, name = "支出 (Expenses)")
        )
    }

    fun getSummary(viewMode: ViewMode, selectedPeriod: String): Summary {
        val targetGroups = getDisplayGroups(viewMode, selectedPeriod)
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
     * 拿到指定期的展示分组：先查按期快照，没有则回退到旧全局字段（兼容首次进入新期 / 未迁移数据）。
     */
    fun getDisplayGroups(viewMode: ViewMode, period: String): List<CategoryGroup> {
        val bucket = bucketFor(viewMode)
        bucket[period]?.let { return it }

        return when (viewMode) {
            ViewMode.DAY -> balanceGroups.ifEmpty {
                groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
            }
            else -> flowGroups.ifEmpty {
                groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
            }
        }
    }

    fun getPeriods(viewMode: ViewMode): List<String> {
        return when (viewMode) {
            ViewMode.YEAR -> years
            ViewMode.MONTH -> months
            ViewMode.DAY -> days
        }.sortedDescending()
    }

    /**
     * 确保 `period` 在 `viewMode` 对应桶里有快照；不存在时按"复制 sourcePeriod → 同模式最近期 → 旧模板 → 默认空模板"的优先级初始化。
     * 复制时保留类目结构与 ID，但金额/数量/单价/备注全部清零并仅保留 `period` 一个 key。
     */
    fun ensureSnapshot(period: String, viewMode: ViewMode, sourcePeriod: String? = null): ReportData {
        if (period.isBlank()) return this
        val isDay = viewMode == ViewMode.DAY
        val bucket = if (isDay) balanceGroupsByPeriod else flowGroupsByPeriod
        if (bucket.containsKey(period)) return this

        val template = pickTemplate(viewMode, period, sourcePeriod)
        val newSnapshot = template.map { group -> group.cloneForPeriod(period) }
        return if (isDay) {
            copy(balanceGroupsByPeriod = balanceGroupsByPeriod + (period to newSnapshot))
        } else {
            copy(flowGroupsByPeriod = flowGroupsByPeriod + (period to newSnapshot))
        }
    }

    /**
     * 更新某期某分组：仅影响该期快照，保证类目互相独立。
     * 若该期快照不存在会先 ensureSnapshot 初始化。
     */
    fun updateGroup(period: String, groupId: String, updater: (CategoryGroup) -> CategoryGroup): ReportData {
        if (period.isBlank()) return this
        val viewMode = viewModeOf(period) ?: return this
        val ensured = ensureSnapshot(period, viewMode)

        val isDay = viewMode == ViewMode.DAY
        val bucket = if (isDay) ensured.balanceGroupsByPeriod else ensured.flowGroupsByPeriod
        val current = bucket[period] ?: return ensured
        val updated = current.map { if (it.id == groupId) updater(it) else it }
        val newBucket = bucket + (period to updated)

        return if (isDay) ensured.copy(balanceGroupsByPeriod = newBucket)
        else ensured.copy(flowGroupsByPeriod = newBucket)
    }

    fun addDay(day: String, sourcePeriod: String? = days.maxOrNull()): ReportData {
        if (days.contains(day)) return this
        val nextDays = (days + day).sortedDescending()
        return copy(days = nextDays).ensureSnapshot(day, ViewMode.DAY, sourcePeriod)
    }

    fun addMonth(month: String, sourcePeriod: String? = months.maxOrNull()): ReportData {
        if (months.contains(month)) return this
        val nextMonths = (months + month).sortedDescending()
        return copy(months = nextMonths).ensureSnapshot(month, ViewMode.MONTH, sourcePeriod)
    }

    fun addYear(year: String, sourcePeriod: String? = years.maxOrNull()): ReportData {
        if (years.contains(year)) return this
        val nextYears = (years + year).sortedDescending()
        return copy(years = nextYears).ensureSnapshot(year, ViewMode.YEAR, sourcePeriod)
    }

    fun getNotes(period: String): List<NoteItem> {
        return if (notesByPeriod.containsKey(period)) {
            notesByPeriod[period].orEmpty()
        } else {
            notes
        }
    }

    fun addNote(period: String, note: NoteItem): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            this[period] = getNotes(period) + note
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }

    fun updateNote(period: String, noteId: String, updater: (NoteItem) -> NoteItem): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            this[period] = getNotes(period).map { if (it.id == noteId) updater(it) else it }
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }

    fun removeNote(period: String, noteId: String): ReportData {
        if (period.isBlank()) return this
        val updatedPeriodNotes = notesByPeriod.toMutableMap().apply {
            this[period] = getNotes(period).filter { it.id != noteId }
        }
        return copy(notesByPeriod = updatedPeriodNotes)
    }

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

    /**
     * 把旧的"全局类目 + 多期 values"数据按期裁剪成独立快照。已迁移过的对象会原样返回。
     */
    fun migrateToPerPeriodIfNeeded(): ReportData {
        if (flowGroupsByPeriod.isNotEmpty() || balanceGroupsByPeriod.isNotEmpty()) return this

        val flowTemplate = flowGroups.ifEmpty {
            groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
        }
        val balanceTemplate = balanceGroups.ifEmpty {
            groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
        }
        if (flowTemplate.isEmpty() && balanceTemplate.isEmpty()) return this

        val flowMigrated = if (flowTemplate.isEmpty()) emptyMap() else {
            (years + months).associateWith { period ->
                flowTemplate.map { it.sliceToPeriod(period) }
            }
        }
        val balanceMigrated = if (balanceTemplate.isEmpty()) emptyMap() else {
            days.associateWith { period ->
                balanceTemplate.map { it.sliceToPeriod(period) }
            }
        }
        return copy(
            flowGroupsByPeriod = flowMigrated,
            balanceGroupsByPeriod = balanceMigrated
        )
    }

    private fun bucketFor(viewMode: ViewMode): Map<String, List<CategoryGroup>> =
        if (viewMode == ViewMode.DAY) balanceGroupsByPeriod else flowGroupsByPeriod

    private fun viewModeOf(period: String): ViewMode? = when {
        days.contains(period) -> ViewMode.DAY
        months.contains(period) -> ViewMode.MONTH
        years.contains(period) -> ViewMode.YEAR
        else -> null
    }

    private fun pickTemplate(viewMode: ViewMode, period: String, sourcePeriod: String?): List<CategoryGroup> {
        val bucket = bucketFor(viewMode)
        sourcePeriod?.takeIf { it != period }?.let { src -> bucket[src]?.let { return it } }
        bucket.entries
            .filter { it.key != period }
            .maxByOrNull { it.key }
            ?.let { return it.value }

        val legacy = when (viewMode) {
            ViewMode.DAY -> balanceGroups.ifEmpty {
                groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
            }
            else -> flowGroups.ifEmpty {
                groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
            }
        }
        if (legacy.isNotEmpty()) return legacy

        return if (viewMode == ViewMode.DAY) defaultBalanceTemplate() else defaultFlowTemplate()
    }
}

/** 把一个 group 整棵树克隆为指定期的初始快照：保持 ID/层级，金额/数量/单价/备注清空到只剩本期 0 值。 */
private fun CategoryGroup.cloneForPeriod(period: String): CategoryGroup =
    copy(items = items.map { it.cloneForPeriod(period) })

private fun ReportItem.cloneForPeriod(period: String): ReportItem = copy(
    values = mapOf(period to 0.0),
    quantities = emptyMap(),
    unitPrices = emptyMap(),
    notesByPeriod = emptyMap(),
    note = null,
    children = children?.map { it.cloneForPeriod(period) }
)

/** 把一个 group 的多期数据裁剪为只保留 `period` 这一期，用于历史数据迁移。 */
private fun CategoryGroup.sliceToPeriod(period: String): CategoryGroup =
    copy(items = items.map { it.sliceToPeriod(period) })

private fun ReportItem.sliceToPeriod(period: String): ReportItem = copy(
    values = values[period]?.let { mapOf(period to it) } ?: emptyMap(),
    quantities = quantities[period]?.let { mapOf(period to it) } ?: emptyMap(),
    unitPrices = unitPrices[period]?.let { mapOf(period to it) } ?: emptyMap(),
    notesByPeriod = notesByPeriod[period]?.let { mapOf(period to it) } ?: emptyMap(),
    note = null,
    children = children?.map { it.sliceToPeriod(period) }
)

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

enum class ViewMode {
    YEAR,
    MONTH,
    DAY
}
