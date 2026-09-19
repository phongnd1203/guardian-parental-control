package app.guardian.android.core.designsystem

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.guardian.android.core.common.ImageCompressor
import kotlinx.coroutines.launch

/**
 * Creates and remembers an avatar image picker launcher that returns compressed WebP/JPEG bytes.
 *
 * @param onImagePicked Callback invoked when an image is successfully chosen and compressed.
 * @param onError Optional error callback if reading or compressing fails.
 * @return A lambda function that triggers the system Photo Picker.
 */
@Composable
fun rememberAvatarPicker(
    onImagePicked: (ByteArray) -> Unit,
    onError: ((Throwable) -> Unit)? = null
): () -> Unit {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val bytes = ImageCompressor.compressImageUri(context, uri)
                    if (bytes != null && bytes.isNotEmpty()) {
                        onImagePicked(bytes)
                    } else {
                        onError?.invoke(IllegalStateException("Failed to compress selected image"))
                    }
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }
    }

    return remember {
        {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}

/**
 * Avatar with an editable camera badge icon at the bottom-right corner.
 */
@Composable
fun EditableAvatar(
    avatarUrl: String?,
    displayName: String,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier.clickable(enabled = !isLoading, onClick = onPickImage),
        contentAlignment = Alignment.Center
    ) {
        AvatarImage(
            avatarUrl = avatarUrl,
            displayName = displayName,
            size = size
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(size * 0.4f),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            // Camera badge at bottom-right
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Đổi ảnh đại diện",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(size * 0.2f)
                )
            }
        }
    }
}
