package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

/**
 * 类别类型枚举
 * - INCOME: 收入
 * - EXPENSE: 支出
 * - ASSET: 资产
 * - LIABILITY: 负债
 */
@Serializable
enum class CategoryType {
    INCOME,
    EXPENSE,
    ASSET,
    LIABILITY;
    
    companion object {
        fun fromString(value: String): CategoryType {
            return when (value.lowercase()) {
                "income" -> INCOME
                "expense" -> EXPENSE
                "asset" -> ASSET
                "liability" -> LIABILITY
                else -> ASSET
            }
        }
    }
}
