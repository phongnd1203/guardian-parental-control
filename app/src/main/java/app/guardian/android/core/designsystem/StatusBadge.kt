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
import app.guardian.android.core.model.DeviceOnlineStatus
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.core.model.ProtectionStatus

@Composable
fun OnlineStatusBadge(
    status: DeviceOnlineStatus,
    modifier: Modifier = Modifier
) {
    val (dotColor, label) = when (status) {
        DeviceOnlineStatus.ONLINE -> Color(0xFF4CAF50) to "Online"
        DeviceOnlineStatus.RECENTLY_ONLINE -> Color(0xFFFF9800) to "Recent"
        DeviceOnlineStatus.OFFLINE -> Color(0xFF9E9E9E) to "Offline"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
