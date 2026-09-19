package app.guardian.android.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer

/**
 * Utility to decode QR codes from Bitmaps and URIs using ZXing.
 */
object QrCodeDecoder {

    /**
     * Decodes QR Code string content from an Android [Bitmap].
     */
    fun decodeBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val result = MultiFormatReader().decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes QR Code content from an image content [Uri].
     */
    fun decodeUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return null
                decodeBitmap(bitmap)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parses an invitation token from raw code or an invitation link URL.
     * Examples:
     * - "https://guardian.example.com/invite/tok-123" -> "tok-123"
     * - "tok-123" -> "tok-123"
     */
    fun extractInvitationToken(input: String): String {
        val trimmed = input.trim()
        val invitePrefix = "/invite/"
        val index = trimmed.indexOf(invitePrefix)
        if (index != -1) {
            val afterPrefix = trimmed.substring(index + invitePrefix.length)
            return afterPrefix.substringBefore('?').substringBefore('#').trimEnd('/')
        }
        return trimmed
    }
}
