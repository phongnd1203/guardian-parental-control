package app.guardian.android.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.guardian.android.ui.MainViewModel
import app.guardian.android.ui.screens.HomeScreen
import app.guardian.android.ui.screens.LoginScreen
import app.guardian.android.ui.screens.OnboardingScreen
import app.guardian.android.ui.screens.RegisterScreen

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
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    viewModel.onAuthSuccess {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    viewModel.onAuthSuccess {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    viewModel.signOut {
                        navController.navigate(Screen.Login.route) {
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
