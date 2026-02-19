package net.metalbrain.paysmart.ui.help.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HelpTopic(
    val title: String,
    val subtitle: String
)

@HiltViewModel
class HelpViewModel @Inject constructor() : ViewModel() {
    data class UiState(
        val topics: List<HelpTopic> = listOf(
            HelpTopic("Account access", "Issues with login, OTP, or password"),
            HelpTopic("Card and wallet", "Top-up, transfer, and wallet balance questions"),
            HelpTopic("Security", "Session lock, passcode, biometrics, and device safety")
        ),
        val supportEmail: String = "support@paysmart.app"
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
}
