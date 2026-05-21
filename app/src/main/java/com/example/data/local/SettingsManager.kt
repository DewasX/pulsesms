package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "messages_settings")

class SettingsManager(private val context: Context) {
    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
        private val KEY_SPAM_PROTECTION = booleanPreferencesKey("spam_protection")
        private val KEY_VERIFIED_BUSINESSES = booleanPreferencesKey("verified_businesses")
        private val KEY_AUTO_OTP = booleanPreferencesKey("auto_otp")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isDynamicThemeEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_DYNAMIC_THEME] ?: true
    }

    val isSpamProtectionEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_SPAM_PROTECTION] ?: true
    }

    val isVerifiedBusinessesEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_VERIFIED_BUSINESSES] ?: true
    }

    val isAutoOtpEnabled: Flow<Boolean> = context.dataStore.data.map { pref ->
        pref[KEY_AUTO_OTP] ?: true
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDynamicThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_DYNAMIC_THEME] = enabled }
    }

    suspend fun setSpamProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_SPAM_PROTECTION] = enabled }
    }

    suspend fun setVerifiedBusinessesEnabled(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_VERIFIED_BUSINESSES] = enabled }
    }

    suspend fun setAutoOtpEnabled(enabled: Boolean) {
        context.dataStore.edit { pref -> pref[KEY_AUTO_OTP] = enabled }
    }
}
