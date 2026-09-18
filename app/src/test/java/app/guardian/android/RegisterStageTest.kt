package app.guardian.android

import app.guardian.android.data.supabase.AuthService
import app.guardian.android.ui.RegisterStage
import app.guardian.android.ui.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAuthService : AuthService {
    var lastEmail: String? = null
    var lastPassword: String? = null
    var lastMetadata: Map<String, String>? = null
    var signUpResult: Result<Unit> = Result.success(Unit)

    override suspend fun signIn(emailInput: String, passwordInput: String): Result<Unit> = Result.success(Unit)

    override suspend fun signUp(
        emailInput: String,
        passwordInput: String,
        metadata: Map<String, String>
    ): Result<Unit> {
        lastEmail = emailInput
        lastPassword = passwordInput
        lastMetadata = metadata
        return signUpResult
    }

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
    override fun hasActiveSession(): Boolean = false
    override fun getCurrentUserEmail(): String? = null
}

class RegisterStageTest {

    private lateinit var fakeAuthService: FakeAuthService
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        fakeAuthService = FakeAuthService()
        viewModel = RegisterViewModel(authService = fakeAuthService)
    }

    @Test
    fun `initial stage is Credentials`() {
        assertEquals(RegisterStage.Credentials, viewModel.uiState.value.stage)
        assertEquals(1, viewModel.uiState.value.stage.stepNumber)
    }

    @Test
    fun `stage 1 fails validation on invalid email or short password`() {
        // Invalid email
        viewModel.updateEmail("not-an-email")
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("password123")
        assertFalse(viewModel.nextStage())
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Short password
        viewModel.updateEmail("test@guardian.app")
        viewModel.updatePassword("123")
        viewModel.updateConfirmPassword("123")
        assertFalse(viewModel.nextStage())
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Password mismatch
        viewModel.updatePassword("password123")
        viewModel.updateConfirmPassword("mismatch456")
        assertFalse(viewModel.nextStage())
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stage 1 advances to stage 2 with valid credentials`() {
        viewModel.updateEmail("alice@guardian.app")
        viewModel.updatePassword("strongPass123")
        viewModel.updateConfirmPassword("strongPass123")

        val advanced = viewModel.nextStage()
        assertTrue(advanced)
        assertEquals(RegisterStage.Profile, viewModel.uiState.value.stage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stage 2 validates name and advances to SecurityReview`() {
        // Prepare stage 1
        viewModel.updateEmail("alice@guardian.app")
        viewModel.updatePassword("strongPass123")
        viewModel.updateConfirmPassword("strongPass123")
        viewModel.nextStage()

        // Missing full name
        assertFalse(viewModel.nextStage())
        assertNotNull(viewModel.uiState.value.errorMessage)

        // Valid profile
        viewModel.updateFullName("Alice Smith")
        viewModel.updateDisplayName("asmith")
        viewModel.updatePhoneNumber("+1234567890")

        val advanced = viewModel.nextStage()
        assertTrue(advanced)
        assertEquals(RegisterStage.SecurityReview, viewModel.uiState.value.stage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `backward navigation steps down stages and preserves input`() {
        // Advance to Stage 3
        viewModel.updateEmail("alice@guardian.app")
        viewModel.updatePassword("strongPass123")
        viewModel.updateConfirmPassword("strongPass123")
        viewModel.nextStage()

        viewModel.updateFullName("Alice Smith")
        viewModel.updateDisplayName("asmith")
        viewModel.nextStage()
        assertEquals(RegisterStage.SecurityReview, viewModel.uiState.value.stage)

        // Back to Stage 2
        val backToProfile = viewModel.previousStage()
        assertTrue(backToProfile)
        assertEquals(RegisterStage.Profile, viewModel.uiState.value.stage)
        assertEquals("Alice Smith", viewModel.uiState.value.fullName)

        // Back to Stage 1
        val backToCredentials = viewModel.previousStage()
        assertTrue(backToCredentials)
        assertEquals(RegisterStage.Credentials, viewModel.uiState.value.stage)
        assertEquals("alice@guardian.app", viewModel.uiState.value.email)

        // Back from Stage 1 returns false (signals exit to Login)
        val exit = viewModel.previousStage()
        assertFalse(exit)
        assertEquals(RegisterStage.Credentials, viewModel.uiState.value.stage)
    }
}
