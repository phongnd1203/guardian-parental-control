package app.guardian.android.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class QrCodeDecoderTest {

    @Test
    fun `extractInvitationToken extracts token from full deep link url`() {
        val url = "https://guardian.example.com/invite/token-abc-123"
        val token = QrCodeDecoder.extractInvitationToken(url)
        assertEquals("token-abc-123", token)
    }

    @Test
    fun `extractInvitationToken extracts token from url with query params and trailing slash`() {
        val url = "https://guardian.example.com/invite/token-xyz/?source=email#target"
        val token = QrCodeDecoder.extractInvitationToken(url)
        assertEquals("token-xyz", token)
    }

    @Test
    fun `extractInvitationToken returns trimmed string for raw code`() {
        val raw = "  INV-998877  "
        val token = QrCodeDecoder.extractInvitationToken(raw)
        assertEquals("INV-998877", token)
    }

    @Test
    fun `extractInvitationToken handles empty string gracefully`() {
        val token = QrCodeDecoder.extractInvitationToken("   ")
        assertEquals("", token)
    }

    @Test
    fun `generate and decode roundtrip with QrCodeGenerator and QrCodeDecoder`() {
        val originalText = "https://guardian.example.com/invite/my-secret-token"
        val bitmap = QrCodeGenerator.generateBitmap(originalText, sizePx = 256)
        if (bitmap != null) {
            val decoded = QrCodeDecoder.decodeBitmap(bitmap)
            assertEquals(originalText, decoded)
            val extractedToken = QrCodeDecoder.extractInvitationToken(decoded ?: "")
            assertEquals("my-secret-token", extractedToken)
        }
    }
}
