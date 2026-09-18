package app.guardian.android.navigation

/**
 * Screen routes and start destination resolution logic.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Auth : Screen("auth")
    data object Home : Screen("home")

    companion object {
        /**
         * Resolves the start destination based on user persistence state:
         * - First launch: Onboarding
         * - Returning user (unauthenticated): Auth
         * - Returning user (authenticated): Home
         */
        fun resolveStartDestination(
            isOnboardingCompleted: Boolean,
            isUserLoggedIn: Boolean
        ): Screen {
            return when {
                !isOnboardingCompleted -> Onboarding
                !isUserLoggedIn -> Auth
                else -> Home
            }
        }
    }
}
