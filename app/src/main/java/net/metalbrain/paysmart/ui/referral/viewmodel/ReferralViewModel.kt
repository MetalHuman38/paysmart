package net.metalbrain.paysmart.ui.referral.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ReferralViewModel @Inject constructor() : ViewModel() {
    data class UiState(
        val referralCode: String = "PAYSMART10",
        val rewardLabel: String = "10.00 GBP",
        val summary: String = "Invite a friend and both of you earn rewards."
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
}
