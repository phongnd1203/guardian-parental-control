package app.guardian.android.ui.screens.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.guardian.android.feature.family.presentation.navigation.FamilyNavHost

@Composable
fun FamilyTab(
    onSignOut: () -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    FamilyNavHost(
        onSignOut = onSignOut,
        onResetAll = onResetAll,
        modifier = modifier
    )
}

