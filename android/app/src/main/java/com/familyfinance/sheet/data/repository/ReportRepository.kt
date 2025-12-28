package com.familyfinance.sheet.data.repository

import com.familyfinance.sheet.data.local.DataStoreManager
import com.familyfinance.sheet.data.model.ReportData
import kotlinx.coroutines.flow.Flow

/**
 * 报表数据仓库
 * 统一管理数据访问
 */
class ReportRepository(private val dataStoreManager: DataStoreManager) {
    
    /**
     * 获取报表数据 Flow
     */
    val reportDataFlow: Flow<ReportData> = dataStoreManager.reportDataFlow
    
    /**
     * 保存报表数据
     */
    suspend fun saveReportData(data: ReportData) {
        dataStoreManager.saveReportData(data)
    }
    
    /**
     * 重置为默认数据
     */
    suspend fun resetToDefault() {
        dataStoreManager.resetToDefault()
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearData() {
        dataStoreManager.clearData()
    }
    
    /**
     * 删除报表（重置为默认）
     */
    suspend fun deleteReport() {
        resetToDefault()
    }
}
