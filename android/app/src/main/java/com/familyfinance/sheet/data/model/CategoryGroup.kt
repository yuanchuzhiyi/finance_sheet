package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

/**
 * 分类组
 * @property id 唯一标识符
 * @property type 类别类型
 * @property name 组名称
 * @property items 该组下的所有科目项
 */
@Serializable
data class CategoryGroup(
    val id: String,
    val type: CategoryType,
    val name: String,
    val items: List<ReportItem> = emptyList()
) {
    /**
     * 计算该组在指定周期的总值
     */
    fun getTotal(period: String): Double {
        return items.sumOf { it.getValue(period) }
    }
    
    /**
     * 添加新科目
     */
    fun addItem(item: ReportItem): CategoryGroup {
        return copy(items = items + item)
    }
    
    /**
     * 更新科目
     */
    fun updateItem(itemId: String, updater: (ReportItem) -> ReportItem): CategoryGroup {
        val updatedItems = items.map { item ->
            item.updateChild(itemId, updater)
        }
        return copy(items = updatedItems)
    }
    
    /**
     * 删除科目
     */
    fun removeItem(itemId: String): CategoryGroup {
        val updatedItems = items
            .filter { it.id != itemId }
            .map { it.removeChild(itemId) }
        return copy(items = updatedItems)
    }
    
    /**
     * 添加子科目到指定父科目
     */
    fun addSubItem(parentId: String, child: ReportItem): CategoryGroup {
        return updateItem(parentId) { parent ->
            parent.addChild(child)
        }
    }
}
