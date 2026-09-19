package app.guardian.android.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMapperTest {

    @Test
    fun `maps all known edge function error codes to domain errors`() {
        assertEquals("UNAUTHORIZED", ErrorMapper.fromCode("UNAUTHORIZED").code)
        assertEquals("FORBIDDEN", ErrorMapper.fromCode("FORBIDDEN").code)
        assertEquals("OWNER_REQUIRED", ErrorMapper.fromCode("OWNER_REQUIRED").code)
        assertEquals("LAST_OWNER_CANNOT_LEAVE", ErrorMapper.fromCode("LAST_OWNER_CANNOT_LEAVE").code)

        assertEquals("FAMILY_NOT_FOUND", ErrorMapper.fromCode("FAMILY_NOT_FOUND").code)
        assertEquals("CHILD_NOT_FOUND", ErrorMapper.fromCode("CHILD_NOT_FOUND").code)
        assertEquals("DEVICE_NOT_FOUND", ErrorMapper.fromCode("DEVICE_NOT_FOUND").code)
        assertEquals("MEMBER_NOT_FOUND", ErrorMapper.fromCode("MEMBER_NOT_FOUND").code)
        assertEquals("INVITATION_NOT_FOUND", ErrorMapper.fromCode("INVITATION_NOT_FOUND").code)

        assertEquals("INVITATION_ALREADY_EXISTS", ErrorMapper.fromCode("INVITATION_ALREADY_EXISTS").code)
        assertEquals("INVITATION_EXPIRED", ErrorMapper.fromCode("INVITATION_EXPIRED").code)
        assertEquals("INVITATION_ALREADY_USED", ErrorMapper.fromCode("INVITATION_ALREADY_USED").code)
        assertEquals("MEMBER_ALREADY_EXISTS", ErrorMapper.fromCode("MEMBER_ALREADY_EXISTS").code)

        assertEquals("PAIRING_CODE_INVALID", ErrorMapper.fromCode("PAIRING_CODE_INVALID").code)
        assertEquals("PAIRING_CODE_EXPIRED", ErrorMapper.fromCode("PAIRING_CODE_EXPIRED").code)
        assertEquals("PAIRING_CODE_ALREADY_USED", ErrorMapper.fromCode("PAIRING_CODE_ALREADY_USED").code)
        assertEquals("DEVICE_ALREADY_PAIRED", ErrorMapper.fromCode("DEVICE_ALREADY_PAIRED").code)

        assertEquals("NETWORK_UNAVAILABLE", ErrorMapper.fromCode("NETWORK_UNAVAILABLE").code)
        assertEquals("SERVER_ERROR", ErrorMapper.fromCode("SERVER_ERROR").code)
        assertEquals("UNKNOWN", ErrorMapper.fromCode("SOME_RANDOM_CODE").code)
    }

    @Test
    fun `error mapper generates friendly UiText`() {
        val expiredError = ErrorMapper.fromCode("PAIRING_CODE_EXPIRED")
        val uiText = ErrorMapper.toUiText(expiredError)
        assertTrue(uiText is UiText.DynamicString)
        val text = (uiText as UiText.DynamicString).value
        assertTrue(text.contains("hết hạn", ignoreCase = true))

        val forbiddenError = ErrorMapper.fromCode("FORBIDDEN")
        val forbiddenUiText = ErrorMapper.toUiText(forbiddenError)
        assertTrue((forbiddenUiText as UiText.DynamicString).value.contains("quyền", ignoreCase = true))
    }
}
