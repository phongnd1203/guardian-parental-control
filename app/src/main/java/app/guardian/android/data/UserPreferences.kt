package app.guardian.android.data

/**
 * Represents the persistent user onboarding and authentication state.
 */
data class UserPreferences(
    val isOnboardingCompleted: Boolean = false,
    val isUserLoggedIn: Boolean = false
)
