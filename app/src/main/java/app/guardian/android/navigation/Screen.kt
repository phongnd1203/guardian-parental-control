package app.guardian.android.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.ui.graphics.vector.ImageVector

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

/**
 * Bottom navigation destinations for authenticated app navigation.
 */
sealed class NavTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Dashboard : NavTab(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    data object Rules : NavTab(
        route = "rules",
        title = "Rules",
        selectedIcon = Icons.Default.Tune,
        unselectedIcon = Icons.Outlined.Tune
    )

    data object Assistant : NavTab(
        route = "assistant",
        title = "Assistant",
        selectedIcon = Icons.Default.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    data object Activity : NavTab(
        route = "activity",
        title = "Activity",
        selectedIcon = Icons.Default.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    data object Family : NavTab(
        route = "family",
        title = "Family",
        selectedIcon = Icons.Default.People,
        unselectedIcon = Icons.Outlined.People
    )

    companion object {
        val items: List<NavTab> = listOf(Dashboard, Rules, Assistant, Activity, Family)
    }
}

