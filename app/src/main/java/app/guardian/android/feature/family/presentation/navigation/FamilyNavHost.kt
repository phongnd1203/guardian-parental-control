package app.guardian.android.feature.family.presentation.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import app.guardian.android.feature.family.presentation.child.AddEditChildScreen
import app.guardian.android.feature.family.presentation.child.AddEditChildViewModel
import app.guardian.android.feature.family.presentation.child.ChildDetailScreen
import app.guardian.android.feature.family.presentation.child.ChildDetailViewModel
import app.guardian.android.feature.family.presentation.create.CreateFamilyScreen
import app.guardian.android.feature.family.presentation.create.CreateFamilyViewModel
import app.guardian.android.feature.family.presentation.device.DeviceDetailScreen
import app.guardian.android.feature.family.presentation.device.DeviceDetailViewModel
import app.guardian.android.feature.family.presentation.device.DevicePermissionsScreen
import app.guardian.android.feature.family.presentation.home.FamilyHomeScreen
import app.guardian.android.feature.family.presentation.home.FamilyHomeViewModel
import app.guardian.android.feature.family.presentation.member.AcceptInviteScreen
import app.guardian.android.feature.family.presentation.member.AcceptInviteViewModel
import app.guardian.android.feature.family.presentation.member.InviteParentScreen
import app.guardian.android.feature.family.presentation.member.InviteParentViewModel
import app.guardian.android.feature.family.presentation.member.MemberDetailScreen
import app.guardian.android.feature.family.presentation.member.MemberDetailViewModel
import app.guardian.android.feature.family.presentation.pairing.PairDeviceScreen
import app.guardian.android.feature.family.presentation.pairing.PairDeviceViewModel
import app.guardian.android.feature.family.presentation.settings.FamilySettingsScreen

@Composable
fun FamilyNavHost(
    onSignOut: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val application = LocalContext.current.applicationContext as Application

    NavHost(
        navController = navController,
        startDestination = FamilyDestinations.HOME,
        modifier = modifier
    ) {
        // Family Home
        composable(FamilyDestinations.HOME) {
            val viewModel: FamilyHomeViewModel = viewModel(
                factory = FamilyHomeViewModel.provideFactory(application)
            )
            FamilyHomeScreen(
                viewModel = viewModel,
                onNavigateToCreateFamily = { navController.navigate(FamilyDestinations.CREATE_FAMILY) },
                onNavigateToAddChild = { navController.navigate(FamilyDestinations.ADD_CHILD) },
                onNavigateToChildDetail = { childId -> navController.navigate(FamilyDestinations.childDetail(childId)) },
                onNavigateToInviteParent = { navController.navigate(FamilyDestinations.INVITE_PARENT) },
                onNavigateToMemberDetail = { memberId -> navController.navigate(FamilyDestinations.memberDetail(memberId)) },
                onNavigateToSettings = { navController.navigate(FamilyDestinations.FAMILY_SETTINGS) }
            )
        }

        // Create Family
        composable(FamilyDestinations.CREATE_FAMILY) {
            val viewModel: CreateFamilyViewModel = viewModel(
                factory = CreateFamilyViewModel.provideFactory(application)
            )
            CreateFamilyScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onFamilyCreated = {
                    navController.popBackStack(FamilyDestinations.HOME, inclusive = false)
                }
            )
        }

        // Add Child
        composable(FamilyDestinations.ADD_CHILD) {
            val viewModel: AddEditChildViewModel = viewModel(
                factory = AddEditChildViewModel.provideFactory(application, childId = null)
            )
            AddEditChildScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { childId ->
                    navController.navigate(FamilyDestinations.childDetail(childId)) {
                        popUpTo(FamilyDestinations.HOME)
                    }
                }
            )
        }

        // Child Detail
        composable(
            route = FamilyDestinations.CHILD_DETAIL,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId").orEmpty()
            val viewModel: ChildDetailViewModel = viewModel(
                key = "child_$childId",
                factory = ChildDetailViewModel.provideFactory(application, childId)
            )
            ChildDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(FamilyDestinations.editChild(id)) },
                onNavigateToPairDevice = { id -> navController.navigate(FamilyDestinations.pairDevice(id)) },
                onNavigateToDeviceDetail = { deviceId -> navController.navigate(FamilyDestinations.deviceDetail(deviceId)) },
                onChildDeleted = { navController.popBackStack(FamilyDestinations.HOME, inclusive = false) }
            )
        }

        // Edit Child
        composable(
            route = FamilyDestinations.EDIT_CHILD,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId").orEmpty()
            val viewModel: AddEditChildViewModel = viewModel(
                key = "edit_child_$childId",
                factory = AddEditChildViewModel.provideFactory(application, childId)
            )
            AddEditChildScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Pair Device
        composable(
            route = FamilyDestinations.PAIR_DEVICE,
            arguments = listOf(navArgument("childId") { type = NavType.StringType })
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString("childId").orEmpty()
            val viewModel: PairDeviceViewModel = viewModel(
                key = "pair_device_$childId",
                factory = PairDeviceViewModel.provideFactory(application, childId)
            )
            PairDeviceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPairingComplete = { navController.popBackStack() }
            )
        }

        // Device Detail
        composable(
            route = FamilyDestinations.DEVICE_DETAIL,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            val viewModel: DeviceDetailViewModel = viewModel(
                key = "device_$deviceId",
                factory = DeviceDetailViewModel.provideFactory(application, deviceId)
            )
            DeviceDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPermissions = { id -> navController.navigate(FamilyDestinations.devicePermissions(id)) },
                onUnpaired = { navController.popBackStack() }
            )
        }

        // Device Permissions
        composable(
            route = FamilyDestinations.DEVICE_PERMISSIONS,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId").orEmpty()
            val viewModel: DeviceDetailViewModel = viewModel(
                key = "device_$deviceId",
                factory = DeviceDetailViewModel.provideFactory(application, deviceId)
            )
            DevicePermissionsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Invite Parent
        composable(FamilyDestinations.INVITE_PARENT) {
            val viewModel: InviteParentViewModel = viewModel(
                factory = InviteParentViewModel.provideFactory(application)
            )
            InviteParentScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onInviteSent = { navController.popBackStack() }
            )
        }

        // Member Detail
        composable(
            route = FamilyDestinations.MEMBER_DETAIL,
            arguments = listOf(navArgument("memberId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getString("memberId").orEmpty()
            val viewModel: MemberDetailViewModel = viewModel(
                key = "member_$memberId",
                factory = MemberDetailViewModel.provideFactory(application, memberId)
            )
            MemberDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onMemberRemoved = { navController.popBackStack() }
            )
        }

        // Family Settings
        composable(FamilyDestinations.FAMILY_SETTINGS) {
            FamilySettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignOut = onSignOut,
                onResetAll = onResetAll
            )
        }

        // Accept Invite (supports deep links)
        composable(
            route = FamilyDestinations.ACCEPT_INVITE,
            arguments = listOf(navArgument("token") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = FamilyDestinations.INVITE_DEEP_LINK_URI_PATTERN }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token").orEmpty()
            val viewModel: AcceptInviteViewModel = viewModel(
                key = "invite_$token",
                factory = AcceptInviteViewModel.provideFactory(application, token)
            )
            AcceptInviteScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAccepted = {
                    navController.navigate(FamilyDestinations.HOME) {
                        popUpTo(FamilyDestinations.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}
