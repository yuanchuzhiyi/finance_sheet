package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

/**
 * 报表科目项
 * @property id 唯一标识符
 * @property name 科目名称
 * @property values 按日期存储金额，key 例如 '2025-01-01'
 * @property quantities 数量，按周期
 * @property unitPrices 单价，按周期
 * @property note 备注
 * @property children 子科目，支持分层汇总
 */
@Serializable
data class ReportItem(
    val id: String,
    val name: String,
    val values: Map<String, Double> = emptyMap(),
    val quantities: Map<String, Double> = emptyMap(),
    val unitPrices: Map<String, Double> = emptyMap(),
    val note: String? = null,
    val children: List<ReportItem>? = null
) {
    /**
     * 获取指定周期的值
     * 如果有子项，返回子项值的总和
     */
    fun getValue(period: String): Double {
        return if (!children.isNullOrEmpty()) {
            children.sumOf { it.getValue(period) }
        } else {
            values[period] ?: 0.0
        }
    }
    
    /**
     * 更新指定周期的值
     */
    fun updateValue(period: String, newValue: Double): ReportItem {
        val qty = quantities[period] ?: 1.0
        val newUnitPrices = unitPrices.toMutableMap().apply {
            this[period] = if (qty != 0.0) newValue / qty else 0.0
        }
        val newValues = values.toMutableMap().apply {
            this[period] = newValue
        }
        return copy(values = newValues, unitPrices = newUnitPrices)
    }
    
    /**
     * 更新备注
     */
    fun updateNote(newNote: String): ReportItem {
        return copy(note = newNote)
    }
    
    /**
     * 重命名科目
     */
    fun rename(newName: String): ReportItem {
        return copy(name = newName)
    }
    
    /**
     * 添加子科目
     */
    fun addChild(child: ReportItem): ReportItem {
        val currentChildren = children ?: emptyList()
        return copy(children = currentChildren + child)
    }
    
    /**
     * 递归更新子项
     */
    fun updateChild(targetId: String, updater: (ReportItem) -> ReportItem): ReportItem {
        if (id == targetId) {
            return updater(this)
        }
        val updatedChildren = children?.map { child ->
            child.updateChild(targetId, updater)
        }
        return copy(children = updatedChildren)
    }
    
    /**
     * 递归删除子项
     */
    fun removeChild(targetId: String): ReportItem {
        val updatedChildren = children
            ?.filter { it.id != targetId }
            ?.map { it.removeChild(targetId) }
        return copy(children = updatedChildren)
    }
}
