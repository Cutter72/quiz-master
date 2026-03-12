package pl.pdgroup.quiz.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    val isHighContrast: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HIGH_CONTRAST_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = enabled
        }
    }

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("theme_mode_dark")
        val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")
    }
}