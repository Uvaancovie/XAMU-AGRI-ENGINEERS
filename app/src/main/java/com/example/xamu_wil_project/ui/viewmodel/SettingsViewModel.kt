package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.UserSettings
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val darkMode: Boolean = false,
    val language: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val settings: StateFlow<UserSettings?> = repository.getUserSettingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repository.getUserSettingsFlow().collect { settings ->
                if (settings != null) {
                    _uiState.value = _uiState.value.copy(
                        darkMode = settings.theme == "dark",
                        language = settings.language
                    )
                }
            }
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            val newSettings = UserSettings(
                theme = if (isDark) "dark" else "light",
                language = _uiState.value.language
            )

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                darkMode = isDark
            )

            val result = repository.saveUserSettings(newSettings)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Theme updated" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            val newSettings = UserSettings(
                theme = if (_uiState.value.darkMode) "dark" else "light",
                language = language
            )

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                language = language
            )

            val result = repository.saveUserSettings(newSettings)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Language updated" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

