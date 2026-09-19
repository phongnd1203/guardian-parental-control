package app.guardian.android.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.guardian.android.core.model.DeviceOnlineStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.ProtectionStatus

@Composable
fun DeviceStatusBadge(
    status: DeviceOnlineStatus,
    modifier: Modifier = Modifier,
    showBackground: Boolean = false
) {
    val (dotColor, label, desc) = when (status) {
        DeviceOnlineStatus.ONLINE -> Triple(Color(0xFF4CAF50), "Online", "Trực tuyến")
        DeviceOnlineStatus.RECENTLY_ONLINE -> Triple(Color(0xFFFF9800), "Recently Online", "Vừa hoạt động")
        DeviceOnlineStatus.OFFLINE -> Triple(Color(0xFF9E9E9E), "Offline", "Ngoại tuyến")
    }

    val content = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (showBackground) 8.dp else 0.dp, vertical = if (showBackground) 4.dp else 0.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "Trạng thái thiết bị: $desc"
            }
            .then(
                if (showBackground) {
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@Composable
fun OnlineStatusBadge(
    status: DeviceOnlineStatus,
    modifier: Modifier = Modifier
) {
    DeviceStatusBadge(
        status = status,
        modifier = modifier,
        showBackground = false
    )
}

@Composable
fun RoleBadge(
    role: FamilyRole,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (role) {
        FamilyRole.OWNER -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "Owner")
        FamilyRole.PARENT -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Parent")
        FamilyRole.VIEWER -> Triple(Color(0xFFF5F5F5), Color(0xFF616161), "Viewer")
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
fun ProtectionStatusBadge(
    status: ProtectionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        ProtectionStatus.ACTIVE -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Protected")
        ProtectionStatus.WARNING -> Triple(Color(0xFFFFF3E0), Color(0xFFF57C00), "Attention")
        ProtectionStatus.DISABLED -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "Disabled")
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    MaterialTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            DeviceStatusBadge(status = DeviceOnlineStatus.ONLINE)
            DeviceStatusBadge(status = DeviceOnlineStatus.RECENTLY_ONLINE)
            DeviceStatusBadge(status = DeviceOnlineStatus.OFFLINE)
            RoleBadge(role = FamilyRole.OWNER)
            RoleBadge(role = FamilyRole.PARENT)
            RoleBadge(role = FamilyRole.VIEWER)
            ProtectionStatusBadge(status = ProtectionStatus.ACTIVE)
            ProtectionStatusBadge(status = ProtectionStatus.WARNING)
            ProtectionStatusBadge(status = ProtectionStatus.DISABLED)
        }
    }
}
