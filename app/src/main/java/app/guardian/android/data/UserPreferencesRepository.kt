package app.guardian.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
        val IS_USER_LOGGED_IN = booleanPreferencesKey("is_user_logged_in")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val isOnboardingCompleted = preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] ?: false
            val isUserLoggedIn = preferences[PreferencesKeys.IS_USER_LOGGED_IN] ?: false
            UserPreferences(
                isOnboardingCompleted = isOnboardingCompleted,
                isUserLoggedIn = isUserLoggedIn
            )
        }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setUserLoggedIn(loggedIn: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_USER_LOGGED_IN] = loggedIn
        }
    }

    suspend fun signOut() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_USER_LOGGED_IN] = false
        }
    }

    suspend fun resetAll() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ONBOARDING_COMPLETED] = false
            preferences[PreferencesKeys.IS_USER_LOGGED_IN] = false
        }
    }
}
