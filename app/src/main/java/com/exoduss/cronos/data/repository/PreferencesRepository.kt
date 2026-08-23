package com.exoduss.cronos.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ONBOARDING_DONE          = booleanPreferencesKey("onboarding_done")
        val PERMISSIONS_REQUESTED    = booleanPreferencesKey("permissions_requested")
        val USER_NAME                = stringPreferencesKey("user_name")
        val DARK_MODE                = booleanPreferencesKey("dark_mode")
        val USE_SYSTEM_THEME         = booleanPreferencesKey("use_system_theme")
        val FAVORITE_TYPE_ID         = stringPreferencesKey("favorite_type_id")
        val DAILY_SUMMARY_ENABLED    = booleanPreferencesKey("daily_summary_enabled")
        val DAILY_SUMMARY_HOUR       = intPreferencesKey("daily_summary_hour")
        val GOOGLE_CONNECTED         = booleanPreferencesKey("google_connected")
        val USER_PLAN                = stringPreferencesKey("user_plan")
        val LAST_BACKUP_TIME         = longPreferencesKey("last_backup_time")
        val BACKUP_MEDIA_ENABLED     = booleanPreferencesKey("backup_media_enabled")
    }

    val isOnboardingDone: Flow<Boolean>      = dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    val isPermissionsRequested: Flow<Boolean> = dataStore.data.map { it[Keys.PERMISSIONS_REQUESTED] ?: false }
    val userName: Flow<String>               = dataStore.data.map { it[Keys.USER_NAME] ?: "" }
    val darkMode: Flow<Boolean>              = dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    val useSystemTheme: Flow<Boolean>        = dataStore.data.map { it[Keys.USE_SYSTEM_THEME] ?: true }
    val favoriteTypeId: Flow<String?>        = dataStore.data.map { it[Keys.FAVORITE_TYPE_ID] }
    val dailySummaryEnabled: Flow<Boolean>   = dataStore.data.map { it[Keys.DAILY_SUMMARY_ENABLED] ?: true }
    val dailySummaryHour: Flow<Int>          = dataStore.data.map { it[Keys.DAILY_SUMMARY_HOUR] ?: 8 }
    val googleConnected: Flow<Boolean>       = dataStore.data.map { it[Keys.GOOGLE_CONNECTED] ?: false }
    val userPlan: Flow<String>               = dataStore.data.map { it[Keys.USER_PLAN] ?: "gratuito" }
    val lastBackupTime: Flow<Long>           = dataStore.data.map { it[Keys.LAST_BACKUP_TIME] ?: 0L }
    val backupMediaEnabled: Flow<Boolean>    = dataStore.data.map { it[Keys.BACKUP_MEDIA_ENABLED] ?: true }

    suspend fun setOnboardingDone()                     = dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    suspend fun setPermissionsRequested()               = dataStore.edit { it[Keys.PERMISSIONS_REQUESTED] = true }
    suspend fun setUserName(name: String)               = dataStore.edit { it[Keys.USER_NAME] = name }
    suspend fun setDarkMode(dark: Boolean)              = dataStore.edit { it[Keys.DARK_MODE] = dark }
    suspend fun setUseSystemTheme(use: Boolean)         = dataStore.edit { it[Keys.USE_SYSTEM_THEME] = use }
    suspend fun setFavoriteTypeId(id: String)           = dataStore.edit { it[Keys.FAVORITE_TYPE_ID] = id }
    suspend fun setDailySummaryEnabled(enabled: Boolean)= dataStore.edit { it[Keys.DAILY_SUMMARY_ENABLED] = enabled }
    suspend fun setDailySummaryHour(hour: Int)          = dataStore.edit { it[Keys.DAILY_SUMMARY_HOUR] = hour }
    suspend fun setGoogleConnected(v: Boolean)          = dataStore.edit { it[Keys.GOOGLE_CONNECTED] = v }
    suspend fun setUserPlan(plan: String)               = dataStore.edit { it[Keys.USER_PLAN] = plan }
    suspend fun setLastBackupTime(ts: Long)             = dataStore.edit { it[Keys.LAST_BACKUP_TIME] = ts }
    suspend fun setBackupMediaEnabled(v: Boolean)       = dataStore.edit { it[Keys.BACKUP_MEDIA_ENABLED] = v }
}
