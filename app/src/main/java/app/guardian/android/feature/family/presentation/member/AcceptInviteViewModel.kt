package app.guardian.android.feature.family.presentation.member

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.usecase.AcceptInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.RefreshFamilyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcceptInviteUiState(
    val token: String,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: UiText? = null
)

class AcceptInviteViewModel(
    application: Application,
    private val token: String,
    private val acceptInvitation: AcceptInvitationUseCase,
    private val refreshFamily: RefreshFamilyUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AcceptInviteUiState(token = token))
    val uiState: StateFlow<AcceptInviteUiState> = _uiState.asStateFlow()

    fun onAcceptInvitation(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = acceptInvitation(token)
            result.fold(
                onSuccess = {
                    refreshFamily()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString(err.message ?: "Failed to accept invitation or invitation is expired.")
                        )
                    }
                }
            )
        }
    }

    companion object {
        fun provideFactory(application: Application, token: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AcceptInviteViewModel(
                        application = application,
                        token = token,
                        acceptInvitation = FamilyDependencyProvider.provideAcceptInvitationUseCase(application),
                        refreshFamily = FamilyDependencyProvider.provideRefreshFamilyUseCase(application)
                    ) as T
                }
            }
    }
}
