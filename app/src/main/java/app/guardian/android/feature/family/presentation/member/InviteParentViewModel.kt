package app.guardian.android.feature.family.presentation.member

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.core.model.FamilyRole
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.usecase.InviteMemberUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InviteParentUiState(
    val email: String = "",
    val selectedRole: FamilyRole = FamilyRole.PARENT,
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isSuccess: Boolean = false
)

class InviteParentViewModel(
    application: Application,
    private val inviteMemberUseCase: InviteMemberUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(InviteParentUiState())
    val uiState: StateFlow<InviteParentUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, errorMessage = null) }
    }

    fun onRoleSelect(role: FamilyRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun onSendInvitation(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Please enter a valid email address"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = inviteMemberUseCase(email, _uiState.value.selectedRole)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString(err.message ?: "Failed to send invitation")
                        )
                    }
                }
            )
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InviteParentViewModel(
                        application = application,
                        inviteMemberUseCase = FamilyDependencyProvider.provideInviteMemberUseCase(application)
                    ) as T
                }
            }
    }
}
