package com.familyfinance.sheet.data.repository

import com.familyfinance.sheet.data.local.DataStoreManager
import com.familyfinance.sheet.data.model.BillBookData
import kotlinx.coroutines.flow.Flow

/**
 * 记账簿数据仓库
 * 统一管理记账簿数据的访问
 */
class BillRepository(private val dataStoreManager: DataStoreManager) {
    
    /**
     * 获取记账簿数据 Flow
     */
    val billBookDataFlow: Flow<BillBookData> = dataStoreManager.billBookDataFlow
    
    /**
     * 保存记账簿数据
     */
    suspend fun saveBillBookData(data: BillBookData) {
        dataStoreManager.saveBillBookData(data)
    }
    
    /**
     * 重置为默认数据
     */
    suspend fun resetToDefault() {
        dataStoreManager.resetBillBookToDefault()
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearData() {
        dataStoreManager.clearBillBookData()
    }
}

