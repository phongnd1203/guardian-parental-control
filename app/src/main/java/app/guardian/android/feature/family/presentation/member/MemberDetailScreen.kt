package app.guardian.android.feature.family.presentation.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.guardian.android.core.designsystem.AvatarImage
import app.guardian.android.core.designsystem.GuardianConfirmDialog
import app.guardian.android.core.designsystem.RoleBadge
import app.guardian.android.core.model.FamilyRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    viewModel: MemberDetailViewModel,
    onNavigateBack: () -> Unit,
    onMemberRemoved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val member = uiState.member

    var showRoleMenu by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Member Details",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading || member == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarImage(
                        avatarUrl = null,
                        displayName = member.email ?: "Member",
                        size = 80.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (uiState.isViewingSelf) "You" else (member.email ?: "Family Member"),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    if (!member.email.isNullOrBlank() && uiState.isViewingSelf) {
                        Text(
                            text = member.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    RoleBadge(role = member.role)

                    Spacer(modifier = Modifier.height(32.dp))

                    // Role details card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Role Permissions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val roleDescription = when (member.role) {
                                FamilyRole.OWNER -> "Primary family administrator. Has full access to manage family settings, members, children profiles, and devices."
                                FamilyRole.PARENT -> "Full management access to create children profiles, pair devices, and configure protection rules."
                                FamilyRole.VIEWER -> "View-only access to child profiles and activity summaries. Cannot alter protection settings."
                            }

                            Text(
                                text = roleDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Change role action if owner and not viewing self
                            if (uiState.isCurrentUserOwner && !uiState.isViewingSelf && !member.isOwner) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box {
                                    OutlinedButton(
                                        onClick = { showRoleMenu = true },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Change Role")
                                    }

                                    DropdownMenu(
                                        expanded = showRoleMenu,
                                        onDismissRequest = { showRoleMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Set as Parent") },
                                            onClick = {
                                                showRoleMenu = false
                                                viewModel.onRoleChange(FamilyRole.PARENT)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Set as Viewer") },
                                            onClick = {
                                                showRoleMenu = false
                                                viewModel.onRoleChange(FamilyRole.VIEWER)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Remove Member Button (Only Owner can remove non-owners)
                if (uiState.isCurrentUserOwner && !uiState.isViewingSelf && !member.isOwner) {
                    Button(
                        onClick = { showRemoveDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Remove from Family",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Remove Confirmation Dialog
        if (showRemoveDialog && member != null) {
            GuardianConfirmDialog(
                title = "Remove ${member.email ?: "Member"}?",
                message = "This user will lose access to all child profiles and family protection data.",
                confirmText = "Remove",
                dismissText = "Cancel",
                isDestructive = true,
                isLoading = uiState.isRemoving,
                onConfirm = {
                    viewModel.onRemoveMember {
                        showRemoveDialog = false
                        onMemberRemoved()
                    }
                },
                onDismiss = { showRemoveDialog = false }
            )
        }
    }
}
