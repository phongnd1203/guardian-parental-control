package app.guardian.android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.guardian.android.navigation.NavTab
import app.guardian.android.ui.screens.tabs.ActivityTab
import app.guardian.android.ui.screens.tabs.AssistantTab
import app.guardian.android.ui.screens.tabs.DashboardTab
import app.guardian.android.ui.screens.tabs.FamilyTab
import app.guardian.android.ui.screens.tabs.RulesTab

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    onResetAll: () -> Unit,
    tabNavController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                androidx.compose.foundation.layout.Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    NavigationBar(
                        windowInsets = NavigationBarDefaults.windowInsets,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavTab.items.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    tabNavController.navigate(tab.route) {
                                        popUpTo(tabNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        maxLines = 1,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab content contains the Navigation Component
            NavHost(
                navController = tabNavController,
                startDestination = NavTab.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(NavTab.Dashboard.route) {
                    DashboardTab()
                }
                composable(NavTab.Rules.route) {
                    RulesTab()
                }
                composable(NavTab.Assistant.route) {
                    AssistantTab()
                }
                composable(NavTab.Activity.route) {
                    ActivityTab()
                }
                composable(NavTab.Family.route) {
                    FamilyTab(
                        onSignOut = onSignOut,
                        onResetAll = onResetAll
                    )
                }
            }
        }
    }
}
