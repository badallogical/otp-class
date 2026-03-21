package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.harekrishna.otpClasses.data.sources.dataStore
import com.harekrishna.otpClasses.data.sources.keys.MessageKeys
import com.harekrishna.otpClasses.data.sources.keys.SettingKeys
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsPreferencesRepository @Inject constructor(
    @param:ApplicationContext val context : Context
) {

    suspend fun saveThemeMode(theme : ThemeMode){
        context.dataStore.edit { preferences ->
            preferences[SettingKeys.THEME_KEY] = theme.name
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val value = preferences[SettingKeys.THEME_KEY]
            try {
                ThemeMode.valueOf(value ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

}