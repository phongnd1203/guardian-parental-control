package app.guardian.android.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Utility to process, scale, and compress avatar images before uploading to Supabase Storage.
 */
object ImageCompressor {

    private const val DEFAULT_MAX_DIMENSION = 512
    private const val DEFAULT_MAX_BYTES = 1024L * 1024L // 1 MB
    private const val INITIAL_QUALITY = 85

    /**
     * Reads an image from [uri], scales it down to [maxDimension] x [maxDimension] preserving aspect ratio,
     * and compresses it into WebP or JPEG format within [maxSizeBytes].
     */
    suspend fun compressImageUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        maxSizeBytes: Long = DEFAULT_MAX_BYTES
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeBitmapFromUri(context, uri, maxDimension) ?: return@withContext null
            val scaledBitmap = scaleBitmap(bitmap, maxDimension)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
            }
            val compressedBytes = compressBitmapToBytes(scaledBitmap, maxSizeBytes)
            scaledBitmap.recycle()
            compressedBytes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compresses an existing [Bitmap] into bytes within [maxSizeBytes].
     */
    suspend fun compressBitmap(
        bitmap: Bitmap,
        maxDimension: Int = DEFAULT_MAX_DIMENSION,
        maxSizeBytes: Long = DEFAULT_MAX_BYTES
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = scaleBitmap(bitmap, maxDimension)
            val bytes = compressBitmapToBytes(scaledBitmap, maxSizeBytes)
            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bytes
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeBitmapFromUri(context: Context, uri: Uri, targetDimension: Int): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val originalWidth = info.size.width
                val originalHeight = info.size.height
                val maxSide = max(originalWidth, originalHeight)
                if (maxSide > targetDimension) {
                    val scale = targetDimension.toFloat() / maxSide
                    decoder.setTargetSize(
                        (originalWidth * scale).toInt().coerceAtLeast(1),
                        (originalHeight * scale).toInt().coerceAtLeast(1)
                    )
                }
            }
        } else {
            // Fallback for pre-P using BitmapFactory
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            val maxSide = max(options.outWidth, options.outHeight)
            var sampleSize = 1
            if (maxSide > targetDimension) {
                sampleSize = maxSide / targetDimension
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(uri)
            val result = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()
            result
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val maxSide = max(width, height)

        if (maxSide <= maxDimension) {
            return bitmap
        }

        val ratio = maxDimension.toFloat() / maxSide
        val targetWidth = (width * ratio).toInt().coerceAtLeast(1)
        val targetHeight = (height * ratio).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun compressBitmapToBytes(bitmap: Bitmap, maxSizeBytes: Long): ByteArray {
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
        }

        var quality = INITIAL_QUALITY
        var stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        var byteArray = stream.toByteArray()

        // If file size exceeds limit, reduce quality iteratively
        while (byteArray.size > maxSizeBytes && quality > 30) {
            stream.close()
            quality -= 15
            stream = ByteArrayOutputStream()
            bitmap.compress(format, quality, stream)
            byteArray = stream.toByteArray()
        }

        stream.close()
        return byteArray
    }
}
