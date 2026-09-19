package app.guardian.android.feature.family.presentation.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.common.UiText
import app.guardian.android.core.network.NetworkMonitor
import app.guardian.android.core.network.SupabaseClientProvider
import app.guardian.android.feature.family.data.repository.FamilyRepositoryImpl
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.feature.family.domain.model.Child
import app.guardian.android.feature.family.domain.model.Family
import app.guardian.android.feature.family.domain.model.FamilyInvitation
import app.guardian.android.feature.family.domain.model.FamilyMember
import app.guardian.android.feature.family.domain.usecase.CancelInvitationUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveChildrenUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveFamilyUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveInvitationsUseCase
import app.guardian.android.feature.family.domain.usecase.ObserveMembersUseCase
import app.guardian.android.feature.family.domain.usecase.RefreshFamilyUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface FamilyUiState {
    data object Loading : FamilyUiState

    data class Content(
        val family: Family,
        val children: List<Child> = emptyList(),
        val members: List<FamilyMember> = emptyList(),
        val pendingInvitations: List<FamilyInvitation> = emptyList(),
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val currentUserId: String? = null
    ) : FamilyUiState

    data class Empty(
        val canCreateFamily: Boolean = true
    ) : FamilyUiState

    data class Error(
        val message: UiText
    ) : FamilyUiState
}

sealed interface FamilyHomeAction {
    data object Refresh : FamilyHomeAction
    data class CancelInvitation(val invitationId: String) : FamilyHomeAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class FamilyHomeViewModel(
    application: Application,
    private val repository: FamilyRepositoryImpl,
    private val observeFamily: ObserveFamilyUseCase,
    private val refreshFamily: RefreshFamilyUseCase,
    private val observeChildren: ObserveChildrenUseCase,
    private val observeMembers: ObserveMembersUseCase,
    private val observeInvitations: ObserveInvitationsUseCase,
    private val cancelInvitation: CancelInvitationUseCase,
    private val networkMonitor: NetworkMonitor
) : AndroidViewModel(application) {

    private val _isRefreshing = MutableStateFlow(false)
    private val _currentUserId = SupabaseClientProvider.auth.currentUserOrNull()?.id

    val uiState: StateFlow<FamilyUiState> = observeFamily()
        .flatMapLatest { family ->
            if (family == null) {
                flowOf(FamilyUiState.Empty(canCreateFamily = true))
            } else {
                // Connect to realtime when family is available
                repository.startRealtimeSync(viewModelScope, family.id)

                combine(
                    observeChildren(family.id),
                    observeMembers(family.id),
                    observeInvitations(family.id),
                    _isRefreshing,
                    networkMonitor.isOnline
                ) { children, members, invitations, isRefreshing, isOnline ->
                    FamilyUiState.Content(
                        family = family,
                        children = children,
                        members = members,
                        pendingInvitations = invitations.filter { it.isPending },
                        isRefreshing = isRefreshing,
                        isOffline = !isOnline,
                        currentUserId = _currentUserId
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FamilyUiState.Loading
        )

    init {
        refresh()
    }

    fun onAction(action: FamilyHomeAction) {
        when (action) {
            is FamilyHomeAction.Refresh -> refresh()
            is FamilyHomeAction.CancelInvitation -> {
                viewModelScope.launch {
                    cancelInvitation(action.invitationId)
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshFamily()
            _isRefreshing.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopRealtimeSync()
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FamilyHomeViewModel(
                        application = application,
                        repository = FamilyDependencyProvider.provideRepository(application),
                        observeFamily = FamilyDependencyProvider.provideObserveFamilyUseCase(application),
                        refreshFamily = FamilyDependencyProvider.provideRefreshFamilyUseCase(application),
                        observeChildren = FamilyDependencyProvider.provideObserveChildrenUseCase(application),
                        observeMembers = FamilyDependencyProvider.provideObserveMembersUseCase(application),
                        observeInvitations = FamilyDependencyProvider.provideObserveInvitationsUseCase(application),
                        cancelInvitation = FamilyDependencyProvider.provideCancelInvitationUseCase(application),
                        networkMonitor = FamilyDependencyProvider.provideNetworkMonitor(application)
                    ) as T
                }
            }
    }
}
