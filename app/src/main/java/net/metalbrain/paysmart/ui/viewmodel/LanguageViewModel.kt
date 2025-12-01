package net.metalbrain.paysmart.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.domain.LanguageRepository
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val repository: LanguageRepository
) : ViewModel() {

    val currentLanguage = repository.getSelectedLanguage()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            repository.setSelectedLanguage(languageCode)
        }
    }
    fun setLanguage(code: String) {
        viewModelScope.launch {
            repository.setSelectedLanguage(code)
        }
    }
}
