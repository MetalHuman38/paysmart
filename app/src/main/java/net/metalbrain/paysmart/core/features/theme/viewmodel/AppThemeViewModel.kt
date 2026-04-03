package net.metalbrain.paysmart.core.features.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
import net.metalbrain.paysmart.core.features.theme.data.AppThemePreferenceRepository
import net.metalbrain.paysmart.core.features.theme.data.AppThemeVariant

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val repository: AppThemePreferenceRepository
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = repository.observeThemeMode()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppThemeMode.DARK
        )

    val themeVariant: StateFlow<AppThemeVariant> = repository.observeThemeVariant()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppThemeVariant.OBSIDIAN
        )

    fun cycleThemeMode() {
        viewModelScope.launch {
            repository.setThemeMode(themeMode.value.next())
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun cycleThemeVariant() {
        viewModelScope.launch {
            repository.setThemeVariant(themeVariant.value.next())
        }
    }

    fun setThemeVariant(variant: AppThemeVariant) {
        viewModelScope.launch {
            repository.setThemeVariant(variant)
        }
    }
}
