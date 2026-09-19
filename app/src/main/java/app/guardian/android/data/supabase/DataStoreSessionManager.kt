package app.guardian.android.data.supabase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private val Context.authDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "guardian_auth_session")

/**
 * A [SessionManager] that persists the Supabase [UserSession] to DataStore so the
 * user stays logged in across app restarts and process kills.
 *
 * The session is stored as a serialized JSON string under the key [KEY_SESSION].
 */
class DataStoreSessionManager(context: Context) : SessionManager {

    private val dataStore = context.applicationContext.authDataStore

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun saveSession(session: UserSession) {
        val encoded = json.encodeToString(session)
        dataStore.edit { prefs -> prefs[KEY_SESSION] = encoded }
    }

    override suspend fun loadSession(): UserSession? {
        return dataStore.data
            .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
            .map { prefs -> prefs[KEY_SESSION] }
            .firstOrNull()
            ?.let { encoded ->
                runCatching { json.decodeFromString<UserSession>(encoded) }.getOrNull()
            }
    }

    override suspend fun deleteSession() {
        dataStore.edit { prefs -> prefs.remove(KEY_SESSION) }
    }

    companion object {
        private val KEY_SESSION = stringPreferencesKey("supabase_user_session")
    }
}
