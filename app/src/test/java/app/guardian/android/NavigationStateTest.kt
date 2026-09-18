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
    fun `returning unauthenticated user resolves to Auth screen`() {
        val startDestination = Screen.resolveStartDestination(
            isOnboardingCompleted = true,
            isUserLoggedIn = false
        )
        assertEquals(Screen.Auth, startDestination)
        assertEquals("auth", startDestination.route)
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
        assertEquals("auth", Screen.Auth.route)
        assertEquals("home", Screen.Home.route)
    }
}
