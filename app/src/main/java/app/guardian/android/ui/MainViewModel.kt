package app.guardian.android.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.guardian.android.core.database.GuardianDatabase
import app.guardian.android.data.UserPreferences
import app.guardian.android.data.UserPreferencesRepository
import app.guardian.android.data.supabase.AuthRepository
import app.guardian.android.feature.family.di.FamilyDependencyProvider
import app.guardian.android.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val initialStartDestination: Screen,
        val preferences: UserPreferences
    ) : MainUiState
}

class MainViewModel(
    application: Application,
    private val repository: UserPreferencesRepository = UserPreferencesRepository(application),
    private val authRepository: AuthRepository = AuthRepository(preferencesRepository = repository)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    init {
        viewModelScope.launch {
            // Read initial preferences from DataStore before NavHost renders
            val initialPrefs = repository.userPreferencesFlow.first()
            val hasFamily = if (initialPrefs.isUserLoggedIn) {
                runCatching {
                    FamilyDependencyProvider.provideRepository(application).checkHasFamily()
                }.getOrDefault(false)
            } else {
                false
            }
            val startDestination = Screen.resolveStartDestination(
                isOnboardingCompleted = initialPrefs.isOnboardingCompleted,
                isUserLoggedIn = initialPrefs.isUserLoggedIn,
                hasFamily = hasFamily
            )
            _uiState.value = MainUiState.Ready(
                initialStartDestination = startDestination,
                preferences = initialPrefs
            )

            // Keep preference state updated
            repository.userPreferencesFlow.collect { prefs ->
                val current = _uiState.value
                if (current is MainUiState.Ready) {
                    _uiState.value = current.copy(preferences = prefs)
                }
            }
        }
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.setOnboardingCompleted(true)
            onSuccess()
        }
    }

    fun onAuthSuccess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.setUserLoggedIn(true)
            onSuccess()
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            repository.signOut()
            runCatching {
                withContext(Dispatchers.IO) {
                    GuardianDatabase.getInstance(getApplication()).clearAllTables()
                }
            }
            onSuccess()
        }
    }

    fun resetAll(onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            repository.resetAll()
            runCatching {
                withContext(Dispatchers.IO) {
                    GuardianDatabase.getInstance(getApplication()).clearAllTables()
                }
            }
            onSuccess()
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(application) as T
                }
            }
    }
}
