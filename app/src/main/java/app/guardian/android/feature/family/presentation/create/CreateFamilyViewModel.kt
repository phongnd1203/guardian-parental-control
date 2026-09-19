package app.guardian.android.feature.family.presentation.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.QrCodeDecoder
import app.guardian.android.core.common.UiText
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.usecase.AcceptInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.CreateFamilyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CreateFamilyTab {
    CREATE,
    JOIN
}

data class CreateFamilyUiState(
    val selectedTab: CreateFamilyTab = CreateFamilyTab.CREATE,
    val familyName: String = "",
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isSuccess: Boolean = false
)

class CreateFamilyViewModel(
    application: Application,
    private val createFamily: CreateFamilyUseCase,
    private val acceptInvitation: AcceptInvitationUseCase,
    private val coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : AndroidViewModel(application) {

    private val scope: kotlinx.coroutines.CoroutineScope
        get() = coroutineScope ?: viewModelScope

    private val _uiState = MutableStateFlow(CreateFamilyUiState())
    val uiState: StateFlow<CreateFamilyUiState> = _uiState.asStateFlow()

    fun onTabChange(tab: CreateFamilyTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    fun onFamilyNameChange(newName: String) {
        _uiState.update { it.copy(familyName = newName, errorMessage = null) }
    }

    fun onInviteCodeChange(newCode: String) {
        _uiState.update { it.copy(inviteCode = newCode, errorMessage = null) }
    }

    fun onQrCodeScanned(qrContent: String, onSuccess: () -> Unit) {
        val token = QrCodeDecoder.extractInvitationToken(qrContent)
        if (token.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Invalid QR code. Could not find invitation token."))
            }
            return
        }
        _uiState.update { it.copy(inviteCode = token, errorMessage = null) }
        onJoinFamily(onSuccess = onSuccess, tokenOverride = token)
    }

    fun onSubmit(onSuccess: () -> Unit) {
        if (_uiState.value.selectedTab == CreateFamilyTab.CREATE) {
            onCreateFamily(onSuccess)
        } else {
            onJoinFamily(onSuccess)
        }
    }

    fun onCreateFamily(onSuccess: () -> Unit) {
        val name = _uiState.value.familyName.trim()
        if (name.length < 2 || name.length > 50) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Family name must be between 2 and 50 characters"))
            }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = createFamily(name)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString(error.message ?: "Failed to create family")
                        )
                    }
                }
            )
        }
    }

    fun onJoinFamily(onSuccess: () -> Unit, tokenOverride: String? = null) {
        val raw = tokenOverride ?: _uiState.value.inviteCode
        val token = QrCodeDecoder.extractInvitationToken(raw)
        if (token.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = UiText.DynamicString("Please enter an invitation code or link"))
            }
            return
        }

        scope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = acceptInvitation(token)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.DynamicString(error.message ?: "Failed to join family. Code may be invalid or expired.")
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
                    return CreateFamilyViewModel(
                        application = application,
                        createFamily = FamilyDependencyProvider.provideCreateFamilyUseCase(application),
                        acceptInvitation = FamilyDependencyProvider.provideAcceptInvitationUseCase(application)
                    ) as T
                }
            }
    }
}
