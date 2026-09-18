package app.guardian.android.data.supabase

import app.guardian.android.data.UserPreferencesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

interface AuthService {
    suspend fun signIn(emailInput: String, passwordInput: String): Result<Unit>
    suspend fun signUp(emailInput: String, passwordInput: String, metadata: Map<String, String> = emptyMap()): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun hasActiveSession(): Boolean
    fun getCurrentUserEmail(): String?
}

class AuthRepository(
    private val client: SupabaseClient = SupabaseConfig.client,
    private val preferencesRepository: UserPreferencesRepository? = null
) : AuthService {

    /**
     * Signs in an existing user with email and password via Supabase Auth.
     */
    override suspend fun signIn(emailInput: String, passwordInput: String): Result<Unit> {
        return runCatching {
            client.auth.signInWith(Email) {
                email = emailInput
                password = passwordInput
            }
            preferencesRepository?.setUserLoggedIn(true)
            Unit
        }
    }

    /**
     * Registers a new user with email, password, and optional user profile metadata.
     */
    override suspend fun signUp(
        emailInput: String,
        passwordInput: String,
        metadata: Map<String, String>
    ): Result<Unit> {
        return runCatching {
            client.auth.signUpWith(Email) {
                email = emailInput
                password = passwordInput
                if (metadata.isNotEmpty()) {
                    data = buildJsonObject {
                        for ((key, value) in metadata) {
                            put(key, value)
                        }
                    }
                }
            }
            preferencesRepository?.setUserLoggedIn(true)
            Unit
        }
    }

    /**
     * Signs the user out from Supabase Auth and updates local preferences.
     */
    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            runCatching { client.auth.signOut() }
            preferencesRepository?.signOut()
            Unit
        }
    }

    /**
     * Checks if there is an active Supabase session.
     */
    override fun hasActiveSession(): Boolean {
        return client.auth.currentSessionOrNull() != null
    }

    /**
     * Returns the current authenticated user's email if available.
     */
    override fun getCurrentUserEmail(): String? {
        return client.auth.currentUserOrNull()?.email
    }
}
