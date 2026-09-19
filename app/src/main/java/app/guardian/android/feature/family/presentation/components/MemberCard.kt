package app.guardian.android.feature.family.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.guardian.android.core.designsystem.AvatarImage
import app.guardian.android.core.designsystem.RoleBadge
import app.guardian.android.feature.family.domain.model.FamilyMember

/**
 * Family member card displaying parent/guardian information,
 * their role badge (Owner, Parent, Viewer), and a "You" badge if matching the current user.
 */
@Composable
fun MemberCard(
    member: FamilyMember,
    modifier: Modifier = Modifier,
    displayName: String? = null,
    avatarUrl: String? = null,
    isCurrentUser: Boolean = false,
    canManage: Boolean = false,
    onClick: (() -> Unit)? = null,
    onManageClick: (() -> Unit)? = null
) {
    val nameToShow = when {
        isCurrentUser && displayName != null -> "$displayName (Bạn)"
        isCurrentUser -> "Bạn"
        !displayName.isNullOrBlank() -> displayName
        !member.email.isNullOrBlank() -> member.email
        else -> "Thành viên gia đình"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Member Avatar
            AvatarImage(
                avatarUrl = avatarUrl,
                displayName = nameToShow,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = nameToShow,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isCurrentUser) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = member.role)

                    if (!member.email.isNullOrBlank() && !isCurrentUser && displayName != null) {
                        Text(
                            text = member.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (canManage && onManageClick != null) {
                IconButton(onClick = onManageClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Tùy chọn quản lý",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MemberCardPreview() {
    MaterialTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            MemberCard(
                member = FamilyMember(
                    id = "m-1",
                    familyId = "family-1",
                    userId = "u-1",
                    role = app.guardian.android.core.model.FamilyRole.OWNER,
                    email = "owner@example.com"
                ),
                displayName = "Nguyen Van A",
                isCurrentUser = true
            )
            MemberCard(
                member = FamilyMember(
                    id = "m-2",
                    familyId = "family-1",
                    userId = "u-2",
                    role = app.guardian.android.core.model.FamilyRole.PARENT,
                    email = "lan@example.com"
                ),
                displayName = "Lan Nguyen",
                isCurrentUser = false,
                canManage = true,
                onManageClick = {}
            )
            MemberCard(
                member = FamilyMember(
                    id = "m-3",
                    familyId = "family-1",
                    userId = "u-3",
                    role = app.guardian.android.core.model.FamilyRole.VIEWER,
                    email = "grandma@example.com"
                ),
                displayName = "Grandma",
                isCurrentUser = false
            )
        }
    }
}
