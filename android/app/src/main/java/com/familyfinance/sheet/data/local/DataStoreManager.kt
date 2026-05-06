package com.familyfinance.sheet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.familyfinance.sheet.data.model.ReportData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "family_finance_datastore"
)

class DataStoreManager(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    companion object {
        private val REPORT_DATA_KEY = stringPreferencesKey("report_data")
    }

    val reportDataFlow: Flow<ReportData> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[REPORT_DATA_KEY]
        val decoded = if (jsonString != null) {
            try {
                json.decodeFromString<ReportData>(jsonString)
            } catch (e: Exception) {
                e.printStackTrace()
                ReportData.default()
            }
        } else {
            ReportData.default()
        }
        decoded.migrateToPerPeriodIfNeeded()
    }

    suspend fun saveReportData(data: ReportData) {
        context.dataStore.edit { preferences ->
            val jsonString = json.encodeToString(data)
            preferences[REPORT_DATA_KEY] = jsonString
        }
    }

    suspend fun clearData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun clearReportData() {
        context.dataStore.edit { preferences ->
            preferences.remove(REPORT_DATA_KEY)
        }
    }

    suspend fun resetToDefault() {
        saveReportData(ReportData.default())
    }
}
