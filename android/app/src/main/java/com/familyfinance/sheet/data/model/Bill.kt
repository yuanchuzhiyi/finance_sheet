package com.familyfinance.sheet.data.model

import kotlinx.serialization.Serializable

/**
 * 账单类型
 */
@Serializable
enum class BillType {
    INCOME,   // 收入
    EXPENSE   // 支出
}

/**
 * 账单分类
 * @property id 分类ID
 * @property name 分类名称
 * @property type 账单类型（收入/支出）
 */
@Serializable
data class BillCategory(
    val id: String,
    val name: String,
    val type: BillType
)

/**
 * 账单
 * @property id 账单ID
 * @property amount 金额
 * @property categoryId 分类ID
 * @property type 账单类型（收入/支出）
 * @property color 账单颜色（ARGB格式，如0xFF6200EE），用于个人区分
 * @property date 日期（格式：yyyy-MM-dd）
 * @property note 备注
 * @property createdAt 创建时间戳
 */
@Serializable
data class Bill(
    val id: String,
    val amount: Double,
    val categoryId: String,
    val type: BillType,
    val color: Long = 0xFF9E9E9EL,  // 默认灰色，ARGB格式
    val date: String,  // yyyy-MM-dd格式
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 记账簿数据
 * @property categories 分类列表
 * @property bills 账单列表
 */
@Serializable
data class BillBookData(
    val categories: List<BillCategory> = emptyList(),
    val bills: List<Bill> = emptyList()
) {
    companion object {
        /**
         * 创建默认记账簿数据（包含一些默认分类）
         */
        fun default(): BillBookData {
            val defaultCategories = listOf(
                // 收入分类
                BillCategory(
                    id = "cat_income_salary",
                    name = "工资收入",
                    type = BillType.INCOME
                ),
                BillCategory(
                    id = "cat_income_bonus",
                    name = "奖金",
                    type = BillType.INCOME
                ),
                BillCategory(
                    id = "cat_income_other",
                    name = "其他收入",
                    type = BillType.INCOME
                ),
                // 支出分类
                BillCategory(
                    id = "cat_expense_food",
                    name = "餐饮美食",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_transport",
                    name = "交通出行",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_shopping",
                    name = "购物消费",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_entertainment",
                    name = "娱乐休闲",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_medical",
                    name = "医疗健康",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_education",
                    name = "教育学习",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_family",
                    name = "亲子",
                    type = BillType.EXPENSE
                ),
                BillCategory(
                    id = "cat_expense_other",
                    name = "其他支出",
                    type = BillType.EXPENSE
                )
            )
            
            return BillBookData(categories = defaultCategories)
        }
    }
    
    /**
     * 添加分类
     */
    fun addCategory(category: BillCategory): BillBookData {
        if (categories.any { it.id == category.id }) {
            return this
        }
        return copy(categories = categories + category)
    }
    
    /**
     * 更新分类
     */
    fun updateCategory(categoryId: String, updater: (BillCategory) -> BillCategory): BillBookData {
        return copy(
            categories = categories.map { 
                if (it.id == categoryId) updater(it) else it 
            }
        )
    }
    
    /**
     * 删除分类
     */
    fun removeCategory(categoryId: String): BillBookData {
        // 删除分类时，同时删除该分类下的所有账单
        return copy(
            categories = categories.filter { it.id != categoryId },
            bills = bills.filter { it.categoryId != categoryId }
        )
    }
    
    /**
     * 添加账单
     */
    fun addBill(bill: Bill): BillBookData {
        if (bills.any { it.id == bill.id }) {
            return this
        }
        return copy(bills = bills + bill)
    }
    
    /**
     * 更新账单
     */
    fun updateBill(billId: String, updater: (Bill) -> Bill): BillBookData {
        return copy(
            bills = bills.map { 
                if (it.id == billId) updater(it) else it 
            }
        )
    }
    
    /**
     * 删除账单
     */
    fun removeBill(billId: String): BillBookData {
        return copy(bills = bills.filter { it.id != billId })
    }
    
    /**
     * 根据分类ID获取分类
     */
    fun getCategory(categoryId: String): BillCategory? {
        return categories.find { it.id == categoryId }
    }
    
    /**
     * 根据类型获取分类列表
     */
    fun getCategoriesByType(type: BillType): List<BillCategory> {
        return categories.filter { it.type == type }
    }
    
    /**
     * 获取指定日期的账单列表
     */
    fun getBillsByDate(date: String): List<Bill> {
        return bills.filter { it.date == date }.sortedByDescending { it.createdAt }
    }
    
    /**
     * 获取指定日期范围的账单列表
     */
    fun getBillsByDateRange(startDate: String, endDate: String): List<Bill> {
        return bills.filter { 
            it.date >= startDate && it.date <= endDate 
        }.sortedByDescending { it.createdAt }
    }
    
    /**
     * 获取指定分类的账单列表
     */
    fun getBillsByCategory(categoryId: String): List<Bill> {
        return bills.filter { it.categoryId == categoryId }.sortedByDescending { it.createdAt }
    }
    
    /**
     * 计算指定日期的总收入
     */
    fun getTotalIncome(date: String): Double {
        return bills.filter { 
            it.date == date && it.type == BillType.INCOME 
        }.sumOf { it.amount }
    }
    
    /**
     * 计算指定日期的总支出
     */
    fun getTotalExpense(date: String): Double {
        return bills.filter { 
            it.date == date && it.type == BillType.EXPENSE 
        }.sumOf { it.amount }
    }
    
    /**
     * 计算指定日期范围的收入
     */
    fun getTotalIncomeByRange(startDate: String, endDate: String): Double {
        return bills.filter { 
            it.date >= startDate && it.date <= endDate && 
            it.type == BillType.INCOME 
        }.sumOf { it.amount }
    }
    
    /**
     * 计算指定日期范围的支出
     */
    fun getTotalExpenseByRange(startDate: String, endDate: String): Double {
        return bills.filter { 
            it.date >= startDate && it.date <= endDate && 
            it.type == BillType.EXPENSE 
        }.sumOf { it.amount }
    }
    
    /**
     * 筛选账单
     * @param categoryId 分类ID（可选）
     * @param type 账单类型（可选）
     * @param color 颜色（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param minAmount 最小金额（可选）
     * @param maxAmount 最大金额（可选）
     */
    fun filterBills(
        categoryId: String? = null,
        type: BillType? = null,
        color: Long? = null,
        startDate: String? = null,
        endDate: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null
    ): List<Bill> {
        return bills.filter { bill ->
            (categoryId == null || bill.categoryId == categoryId) &&
            (type == null || bill.type == type) &&
            (color == null || bill.color == color) &&
            (startDate == null || bill.date >= startDate) &&
            (endDate == null || bill.date <= endDate) &&
            (minAmount == null || bill.amount >= minAmount) &&
            (maxAmount == null || bill.amount <= maxAmount)
        }.sortedByDescending { it.createdAt }
    }
}

