package com.familyfinance.sheet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.familyfinance.sheet.data.model.BillBookData
import com.familyfinance.sheet.data.model.ReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "family_finance_datastore"
)

/**
 * DataStore 管理器
 * 负责本地数据的持久化存储
 */
class DataStoreManager(private val context: Context) {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
    
    companion object {
        private val REPORT_DATA_KEY = stringPreferencesKey("report_data")
        private val BILL_BOOK_DATA_KEY = stringPreferencesKey("bill_book_data")
    }
    
    /**
     * 获取报表数据 Flow
     */
    val reportDataFlow: Flow<ReportData> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[REPORT_DATA_KEY]
        val decoded = if (jsonString != null) {
            try {
                json.decodeFromString<ReportData>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                // 解析失败时返回空数据，而不是默认模版
                ReportData.default()
            }
        } else {
            // 数据不存在时返回空数据，而不是默认模版
            ReportData.default()
        }
        decoded.migrateToPerPeriodIfNeeded()
    }
    
    /**
     * 保存报表数据
     */
    suspend fun saveReportData(data: ReportData) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(data)
            preferences[REPORT_DATA_KEY] = jsonString
        }
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * 清除报表数据（只清除报表，保留记账簿数据）
     */
    suspend fun clearReportData() {
        context.dataStore.edit { preferences ->
            preferences.remove(REPORT_DATA_KEY)
        }
    }
    
    /**
     * 重置为默认数据
     */
    suspend fun resetToDefault() {
        saveReportData(ReportData.default())
    }
    
    /**
     * 获取记账簿数据 Flow
     */
    val billBookDataFlow: Flow<BillBookData> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[BILL_BOOK_DATA_KEY]
        if (jsonString != null) {
            try {
                json.decodeFromString<BillBookData>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                BillBookData.default()
            }
        } else {
            BillBookData.default()
        }
    }
    
    /**
     * 保存记账簿数据
     */
    suspend fun saveBillBookData(data: BillBookData) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(data)
            preferences[BILL_BOOK_DATA_KEY] = jsonString
        }
    }
    
    /**
     * 清除记账簿数据
     */
    suspend fun clearBillBookData() {
        context.dataStore.edit { preferences ->
            preferences.remove(BILL_BOOK_DATA_KEY)
        }
    }
    
    /**
     * 重置记账簿为默认数据
     */
    suspend fun resetBillBookToDefault() {
        saveBillBookData(BillBookData.default())
    }
}
