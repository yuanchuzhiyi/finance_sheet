package com.familyfinance.sheet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familyfinance.sheet.data.local.DataStoreManager
import com.familyfinance.sheet.data.model.Bill
import com.familyfinance.sheet.data.model.BillBookData
import com.familyfinance.sheet.data.model.BillCategory
import com.familyfinance.sheet.data.model.BillType
import com.familyfinance.sheet.data.repository.BillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 记账簿 ViewModel
 */
class BillViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dataStoreManager = DataStoreManager(application)
    private val repository = BillRepository(dataStoreManager)
    
    // 记账簿数据
    val billBookData: StateFlow<BillBookData> = repository.billBookDataFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        BillBookData.default()
    )
    
    // 选中的日期（格式：yyyy-MM-dd）
    private val _selectedDate = MutableStateFlow(getCurrentDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // 是否仅查看选中日期
    private val _filterBySelectedDate = MutableStateFlow(false)
    val filterBySelectedDate: StateFlow<Boolean> = _filterBySelectedDate.asStateFlow()
    
    // 选中的账单类型（用于筛选）
    private val _selectedBillType = MutableStateFlow<BillType?>(null)
    val selectedBillType: StateFlow<BillType?> = _selectedBillType.asStateFlow()
    
    // 筛选条件
    private val _filterCategoryId = MutableStateFlow<String?>(null)
    val filterCategoryId: StateFlow<String?> = _filterCategoryId.asStateFlow()
    
    private val _filterColor = MutableStateFlow<Long?>(null)
    val filterColor: StateFlow<Long?> = _filterColor.asStateFlow()
    
    private val _filterStartDate = MutableStateFlow<String?>(null)
    val filterStartDate: StateFlow<String?> = _filterStartDate.asStateFlow()
    
    private val _filterEndDate = MutableStateFlow<String?>(null)
    val filterEndDate: StateFlow<String?> = _filterEndDate.asStateFlow()
    
    private val _filterMinAmount = MutableStateFlow<Double?>(null)
    val filterMinAmount: StateFlow<Double?> = _filterMinAmount.asStateFlow()
    
    private val _filterMaxAmount = MutableStateFlow<Double?>(null)
    val filterMaxAmount: StateFlow<Double?> = _filterMaxAmount.asStateFlow()
    
    // 所有账单列表（应用筛选，按时间从新到旧排序）
    val currentDateBills: StateFlow<List<Bill>> = combine(
        billBookData, selectedDate, filterBySelectedDate, selectedBillType,
        _filterCategoryId, _filterColor, _filterStartDate, _filterEndDate,
        _filterMinAmount, _filterMaxAmount
    ) { values ->
        val data = values[0] as BillBookData
        val selectedDate = values[1] as String
        val selectedDateOnly = values[2] as Boolean
        val type = values[3] as BillType?
        val categoryId = values[4] as String?
        val color = values[5] as Long?
        val startDate = values[6] as String?
        val endDate = values[7] as String?
        val minAmount = values[8] as Double?
        val maxAmount = values[9] as Double?

        // 快速日期聚焦会覆盖区间日期筛选，避免两个日期筛选叠加后语义混乱。
        val actualStartDate = if (selectedDateOnly) selectedDate else startDate
        val actualEndDate = if (selectedDateOnly) selectedDate else endDate

        data.filterBills(
            categoryId = categoryId,
            type = type,
            color = color,
            startDate = actualStartDate,
            endDate = actualEndDate,
            minAmount = minAmount,
            maxAmount = maxAmount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 收入总额（基于筛选后的账单列表）
    val currentDateIncome: StateFlow<Double> = currentDateBills.map { bills ->
        bills.filter { it.type == BillType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // 支出总额（基于筛选后的账单列表）
    val currentDateExpense: StateFlow<Double> = currentDateBills.map { bills ->
        bills.filter { it.type == BillType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // 当前日期的结余
    val currentDateBalance: StateFlow<Double> = combine(
        currentDateIncome, currentDateExpense
    ) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    
    // 分类列表（按类型过滤）
    val filteredCategories: StateFlow<List<BillCategory>> = combine(
        billBookData, selectedBillType
    ) { data, type ->
        if (type == null) {
            data.categories
        } else {
            data.getCategoriesByType(type)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    /**
     * 获取当前日期（格式：yyyy-MM-dd）
     */
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    /**
     * 设置选中的日期
     */
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun focusOnDate(date: String) {
        _selectedDate.value = date
        _filterBySelectedDate.value = true
    }

    fun showAllDates() {
        _filterBySelectedDate.value = false
    }
    
    /**
     * 设置选中的账单类型
     */
    fun setSelectedBillType(type: BillType?) {
        _selectedBillType.value = type
    }
    
    /**
     * 设置筛选条件
     */
    fun setFilterCategory(categoryId: String?) {
        _filterCategoryId.value = categoryId
    }
    
    fun setFilterColor(color: Long?) {
        _filterColor.value = color
    }
    
    fun setFilterDateRange(startDate: String?, endDate: String?) {
        _filterStartDate.value = startDate
        _filterEndDate.value = endDate
    }
    
    fun setFilterAmountRange(minAmount: Double?, maxAmount: Double?) {
        _filterMinAmount.value = minAmount
        _filterMaxAmount.value = maxAmount
    }
    
    fun clearFilters() {
        _filterBySelectedDate.value = false
        _selectedBillType.value = null
        _filterCategoryId.value = null
        _filterColor.value = null
        _filterStartDate.value = null
        _filterEndDate.value = null
        _filterMinAmount.value = null
        _filterMaxAmount.value = null
    }
    
    /**
     * 添加账单
     */
    fun addBill(amount: Double, categoryId: String, type: BillType, color: Long, date: String, note: String = "") {
        viewModelScope.launch {
            val data = billBookData.value
            val bill = Bill(
                id = "bill_${System.currentTimeMillis()}",
                amount = amount,
                categoryId = categoryId,
                type = type,
                color = color,
                date = date,
                note = note
            )
            val newData = data.addBill(bill)
            repository.saveBillBookData(newData)
            _selectedDate.value = date
        }
    }
    
    /**
     * 更新账单
     */
    fun updateBill(billId: String, amount: Double? = null, categoryId: String? = null, 
                   type: BillType? = null, color: Long? = null,
                   date: String? = null, note: String? = null) {
        viewModelScope.launch {
            val data = billBookData.value
            val newData = data.updateBill(billId) { bill ->
                bill.copy(
                    amount = amount ?: bill.amount,
                    categoryId = categoryId ?: bill.categoryId,
                    type = type ?: bill.type,
                    color = color ?: bill.color,
                    date = date ?: bill.date,
                    note = note ?: bill.note
                )
            }
            repository.saveBillBookData(newData)
        }
    }
    
    /**
     * 删除账单
     */
    fun deleteBill(billId: String) {
        viewModelScope.launch {
            val data = billBookData.value
            val newData = data.removeBill(billId)
            repository.saveBillBookData(newData)
        }
    }
    
    /**
     * 添加分类
     */
    fun addCategory(name: String, type: BillType) {
        viewModelScope.launch {
            val data = billBookData.value
            val category = BillCategory(
                id = "cat_${System.currentTimeMillis()}",
                name = name,
                type = type
            )
            val newData = data.addCategory(category)
            repository.saveBillBookData(newData)
        }
    }
    
    /**
     * 更新分类
     */
    fun updateCategory(categoryId: String, name: String? = null, type: BillType? = null) {
        viewModelScope.launch {
            val data = billBookData.value
            val newData = data.updateCategory(categoryId) { category ->
                category.copy(
                    name = name ?: category.name,
                    type = type ?: category.type
                )
            }
            repository.saveBillBookData(newData)
        }
    }
    
    /**
     * 删除分类
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val data = billBookData.value
            val newData = data.removeCategory(categoryId)
            repository.saveBillBookData(newData)
        }
    }
    
    /**
     * 保存数据
     */
    fun saveData() {
        viewModelScope.launch {
            repository.saveBillBookData(billBookData.value)
        }
    }
    
    /**
     * 重置为默认数据
     */
    fun resetToDefault() {
        viewModelScope.launch {
            repository.resetToDefault()
        }
    }
}
