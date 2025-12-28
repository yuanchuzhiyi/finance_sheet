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
 * @property notes 其他信息（不参与汇总）
 */
@Serializable
data class ReportData(
    val years: List<String> = emptyList(),
    val months: List<String> = emptyList(),
    val days: List<String> = emptyList(),
    val groups: List<CategoryGroup> = emptyList(),
    val flowGroups: List<CategoryGroup> = emptyList(),
    val balanceGroups: List<CategoryGroup> = emptyList(),
    val notes: List<NoteItem> = emptyList()
) {
    companion object {
        /**
         * 创建默认报表数据
         */
        fun default(): ReportData {
            val defaultPeriods = listOf("2025")
            val defaultMonths = listOf("2025-01", "2025-02")
            val defaultDays = listOf("2025-01-01")
            
            val flowGroups = listOf(
                CategoryGroup(
                    id = "income",
                    type = CategoryType.INCOME,
                    name = "收入 (Income)",
                    items = listOf(
                        ReportItem(
                            id = "inc_1",
                            name = "工资收入",
                            values = mapOf("2025" to 240000.0, "2025-01" to 20000.0, "2025-02" to 20000.0)
                        ),
                        ReportItem(
                            id = "inc_2",
                            name = "公积金收入",
                            values = mapOf("2025" to 6000.0, "2025-02" to 500.0)
                        ),
                        ReportItem(id = "inc_3", name = "股票投资收入"),
                        ReportItem(id = "inc_4", name = "期货投资收入"),
                        ReportItem(id = "inc_5", name = "黄金投资收入"),
                        ReportItem(id = "inc_6", name = "白银投资收入"),
                        ReportItem(id = "inc_7", name = "铑投资收入"),
                        ReportItem(id = "inc_8", name = "加密货币投资收入"),
                        ReportItem(id = "inc_9", name = "其他投资收入")
                    )
                ),
                CategoryGroup(
                    id = "expense",
                    type = CategoryType.EXPENSE,
                    name = "支出 (Expenses)",
                    items = listOf(
                        ReportItem(
                            id = "exp_1",
                            name = "房贷/房租",
                            values = mapOf("2025" to 60000.0, "2025-01" to 5000.0, "2025-02" to 5000.0)
                        ),
                        ReportItem(
                            id = "exp_2",
                            name = "餐饮美食",
                            values = mapOf("2025" to 36000.0, "2025-01" to 3000.0, "2025-02" to 2800.0)
                        ),
                        ReportItem(
                            id = "exp_3",
                            name = "交通出行",
                            values = mapOf("2025" to 9600.0, "2025-02" to 800.0)
                        )
                    )
                )
            )
            
            val balanceGroups = listOf(
                CategoryGroup(
                    id = "asset",
                    type = CategoryType.ASSET,
                    name = "资产 (Assets)",
                    items = listOf(
                        ReportItem(id = "ast_cash", name = "现金", values = mapOf("2025-01-01" to 20000.0)),
                        ReportItem(id = "ast_recv", name = "应收借款", values = mapOf("2025-01-01" to 50000.0)),
                        ReportItem(id = "ast_int", name = "累计利息", values = mapOf("2025-01-01" to 500.0)),
                        ReportItem(id = "ast_gold", name = "黄金资产", values = mapOf("2025-01-01" to 50000.0)),
                        ReportItem(id = "ast_stock", name = "股票资产", values = mapOf("2025-01-01" to 110000.0)),
                        ReportItem(id = "ast_rhod", name = "铑实物", values = mapOf("2025-01-01" to 30000.0)),
                        ReportItem(id = "ast_crypto", name = "加密货币", values = mapOf("2025-01-01" to 38000.0)),
                        ReportItem(id = "ast_fund", name = "公积金", values = mapOf("2025-01-01" to 78000.0)),
                        ReportItem(id = "ast_savegold", name = "积存金", values = mapOf("2025-01-01" to 15000.0)),
                        ReportItem(id = "ast_futures", name = "期货资产", values = mapOf("2025-01-01" to 24000.0))
                    )
                ),
                CategoryGroup(
                    id = "liability",
                    type = CategoryType.LIABILITY,
                    name = "负债 (Liabilities)",
                    items = listOf(
                        ReportItem(id = "lia_card", name = "信用卡未还", values = mapOf("2025-01-01" to 2500.0)),
                        ReportItem(id = "lia_debt", name = "欠款", values = mapOf("2025-01-01" to 50000.0))
                    )
                )
            )
            
            return ReportData(
                years = defaultPeriods,
                months = defaultMonths,
                days = defaultDays,
                groups = flowGroups + balanceGroups,
                flowGroups = flowGroups,
                balanceGroups = balanceGroups,
                notes = listOf(
                    NoteItem(id = "note_1", label = "养老保险总缴纳额", value = "200000")
                )
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
     */
    fun getDisplayGroups(viewMode: ViewMode): List<CategoryGroup> {
        return when (viewMode) {
            ViewMode.DAY -> balanceGroups.ifEmpty {
                groups.filter { it.type == CategoryType.ASSET || it.type == CategoryType.LIABILITY }
            }
            else -> flowGroups.ifEmpty {
                groups.filter { it.type == CategoryType.INCOME || it.type == CategoryType.EXPENSE }
            }
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
        return copy(days = days + day)
    }
    
    /**
     * 添加新月份
     */
    fun addMonth(month: String): ReportData {
        if (months.contains(month)) return this
        return copy(months = months + month)
    }
    
    /**
     * 添加新年份
     */
    fun addYear(year: String): ReportData {
        if (years.contains(year)) return this
        return copy(years = years + year)
    }
    
    /**
     * 添加附注
     */
    fun addNote(note: NoteItem): ReportData {
        return copy(notes = notes + note)
    }
    
    /**
     * 更新附注
     */
    fun updateNote(noteId: String, updater: (NoteItem) -> NoteItem): ReportData {
        return copy(notes = notes.map { if (it.id == noteId) updater(it) else it })
    }
    
    /**
     * 删除附注
     */
    fun removeNote(noteId: String): ReportData {
        return copy(notes = notes.filter { it.id != noteId })
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

/**
 * 视图模式
 */
enum class ViewMode {
    YEAR,
    MONTH,
    DAY
}
