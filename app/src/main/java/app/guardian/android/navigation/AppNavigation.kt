package app.guardian.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.guardian.android.ui.MainViewModel
import app.guardian.android.ui.screens.AuthScreen
import app.guardian.android.ui.screens.HomeScreen
import app.guardian.android.ui.screens.OnboardingScreen

@Composable
fun AppNavigation(
    startDestination: Screen,
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleteOnboarding = {
                    viewModel.completeOnboarding {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    viewModel.signIn {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    viewModel.signOut {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onResetAll = {
                    viewModel.resetAll {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Home.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
    }
}
