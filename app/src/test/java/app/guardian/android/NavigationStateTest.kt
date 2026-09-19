package app.guardian.android

import app.guardian.android.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationStateTest {

    @Test
    fun `first launch resolves to Onboarding screen`() {
        val startDestination = Screen.resolveStartDestination(
            isOnboardingCompleted = false,
            isUserLoggedIn = false
        )
        assertEquals(Screen.Onboarding, startDestination)
        assertEquals("onboarding", startDestination.route)
    }

    @Test
    fun `returning unauthenticated user resolves to Login screen`() {
        val startDestination = Screen.resolveStartDestination(
            isOnboardingCompleted = true,
            isUserLoggedIn = false
        )
        assertEquals(Screen.Login, startDestination)
        assertEquals("login", startDestination.route)
    }

    @Test
    fun `returning authenticated user resolves to Home screen`() {
        val startDestination = Screen.resolveStartDestination(
            isOnboardingCompleted = true,
            isUserLoggedIn = true
        )
        assertEquals(Screen.Home, startDestination)
        assertEquals("home", startDestination.route)
    }

    @Test
    fun `uncompleted onboarding always resolves to Onboarding regardless of login flag`() {
        val startDestination = Screen.resolveStartDestination(
            isOnboardingCompleted = false,
            isUserLoggedIn = true
        )
        assertEquals(Screen.Onboarding, startDestination)
    }

    @Test
    fun `screen routes are uniquely and accurately configured`() {
        assertEquals("onboarding", Screen.Onboarding.route)
        assertEquals("login", Screen.Login.route)
        assertEquals("register", Screen.Register.route)
        assertEquals("home", Screen.Home.route)
    }

    @Test
    fun `nav tabs are uniquely and accurately configured with 5 destinations`() {
        val tabs = app.guardian.android.navigation.NavTab.items
        assertEquals(5, tabs.size)
        assertEquals("dashboard", app.guardian.android.navigation.NavTab.Dashboard.route)
        assertEquals("rules", app.guardian.android.navigation.NavTab.Rules.route)
        assertEquals("assistant", app.guardian.android.navigation.NavTab.Assistant.route)
        assertEquals("activity", app.guardian.android.navigation.NavTab.Activity.route)
        assertEquals("family", app.guardian.android.navigation.NavTab.Family.route)

        assertEquals("Dashboard", app.guardian.android.navigation.NavTab.Dashboard.title)
        assertEquals("Rules & Limits", app.guardian.android.navigation.NavTab.Rules.title)
        assertEquals("Assistant", app.guardian.android.navigation.NavTab.Assistant.title)
        assertEquals("Activity", app.guardian.android.navigation.NavTab.Activity.title)
        assertEquals("Family", app.guardian.android.navigation.NavTab.Family.title)

        val uniqueRoutes = tabs.map { it.route }.toSet()
        assertEquals(5, uniqueRoutes.size)
    }
}

