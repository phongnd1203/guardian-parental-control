package app.guardian.android.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.guardian.android.feature.family.presentation.create.CreateFamilyScreen
import app.guardian.android.feature.family.presentation.create.CreateFamilyViewModel
import app.guardian.android.ui.MainViewModel
import app.guardian.android.ui.screens.MainScreen
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
                onGetStarted = {
                    viewModel.completeOnboarding {
                        navController.navigate(Screen.Register.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                        }
                    }
                },
                onSignIn = {
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
                onLoginSuccess = { hasFamily ->
                    viewModel.onAuthSuccess {
                        val destination = if (hasFamily) Screen.Home.route else Screen.CreateFamily.route
                        navController.navigate(destination) {
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
                        navController.navigate(Screen.CreateFamily.route) {
                            popUpTo(Screen.Register.route) {
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

        composable(Screen.CreateFamily.route) {
            val context = LocalContext.current.applicationContext as Application
            val createFamilyViewModel: CreateFamilyViewModel = viewModel(
                factory = CreateFamilyViewModel.provideFactory(context)
            )
            CreateFamilyScreen(
                viewModel = createFamilyViewModel,
                onNavigateBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CreateFamily.route) {
                            inclusive = true
                        }
                    }
                },
                onFamilyCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CreateFamily.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            MainScreen(
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
