package app.guardian.android.navigation

/**
 * Screen routes and start destination resolution logic.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")

    companion object {
        /**
         * Resolves the start destination based on user persistence state:
         * - First launch: Onboarding
         * - Returning user (unauthenticated): Login
         * - Returning user (authenticated): Home
         */
        fun resolveStartDestination(
            isOnboardingCompleted: Boolean,
            isUserLoggedIn: Boolean
        ): Screen {
            return when {
                !isOnboardingCompleted -> Onboarding
                !isUserLoggedIn -> Login
                else -> Home
            }
        }
    }
}
