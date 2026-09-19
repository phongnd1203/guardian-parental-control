package app.guardian.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.guardian.android.data.supabase.AuthRepository
import app.guardian.android.data.supabase.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RegisterStage(val stepNumber: Int, val title: String) {
    Credentials(1, "Parent Account"),
    Profile(2, "Parent Profile"),
    SecurityReview(3, "Family Safety & Review")
}

data class RegisterUiState(
    val stage: RegisterStage = RegisterStage.Credentials,
    // Stage 1: Credentials
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    // Stage 2: Profile
    val fullName: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    // Stage 3: Security & Consent
    val enableThreatAlerts: Boolean = true,
    val enableTwoFactor: Boolean = true,
    val agreeToTerms: Boolean = true,
    // Status
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val authService: AuthService = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateEmail(value: String) = _uiState.update { it.copy(email = value, errorMessage = null) }
    fun updatePassword(value: String) = _uiState.update { it.copy(password = value, errorMessage = null) }
    fun updateConfirmPassword(value: String) = _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    fun updateFullName(value: String) = _uiState.update { it.copy(fullName = value, errorMessage = null) }
    fun updateDisplayName(value: String) = _uiState.update { it.copy(displayName = value, errorMessage = null) }
    fun updatePhoneNumber(value: String) = _uiState.update { it.copy(phoneNumber = value, errorMessage = null) }
    fun updateThreatAlerts(value: Boolean) = _uiState.update { it.copy(enableThreatAlerts = value) }
    fun updateTwoFactor(value: Boolean) = _uiState.update { it.copy(enableTwoFactor = value) }
    fun updateAgreeToTerms(value: Boolean) = _uiState.update { it.copy(agreeToTerms = value, errorMessage = null) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    /**
     * Advances to the next registration stage if validation passes.
     * Returns true if stage advanced.
     */
    fun nextStage(): Boolean {
        val state = _uiState.value
        when (state.stage) {
            RegisterStage.Credentials -> {
                if (state.email.isBlank() || !state.email.contains("@") || !state.email.contains(".")) {
                    _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
                    return false
                }
                if (state.password.length < 6) {
                    _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters long.") }
                    return false
                }
                if (state.password != state.confirmPassword) {
                    _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
                    return false
                }
                _uiState.update { it.copy(stage = RegisterStage.Profile, errorMessage = null) }
                return true
            }
            RegisterStage.Profile -> {
                if (state.fullName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
                    return false
                }
                if (state.displayName.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "Please choose a display name.") }
                    return false
                }
                _uiState.update { it.copy(stage = RegisterStage.SecurityReview, errorMessage = null) }
                return true
            }
            RegisterStage.SecurityReview -> {
                return false
            }
        }
    }

    /**
     * Returns to the previous registration stage.
     * Returns true if moved back to a previous stage, false if already at Stage 1.
     */
    fun previousStage(): Boolean {
        val state = _uiState.value
        return when (state.stage) {
            RegisterStage.SecurityReview -> {
                _uiState.update { it.copy(stage = RegisterStage.Profile, errorMessage = null) }
                true
            }
            RegisterStage.Profile -> {
                _uiState.update { it.copy(stage = RegisterStage.Credentials, errorMessage = null) }
                true
            }
            RegisterStage.Credentials -> false
        }
    }

    /**
     * Completes registration using Supabase Auth with collected multi-stage metadata.
     */
    fun completeRegistration(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.agreeToTerms) {
            _uiState.update { it.copy(errorMessage = "You must agree to the Family Privacy Policy & Terms to continue.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val metadata = mapOf(
                "full_name" to state.fullName,
                "display_name" to state.displayName,
                "phone_number" to state.phoneNumber,
                "enable_threat_alerts" to state.enableThreatAlerts.toString(),
                "enable_two_factor" to state.enableTwoFactor.toString()
            )

            val result = authService.signUp(
                emailInput = state.email.trim(),
                passwordInput = state.password,
                metadata = metadata
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Registration failed. Check your connection or credentials."
                        )
                    }
                }
            )
        }
    }
}
