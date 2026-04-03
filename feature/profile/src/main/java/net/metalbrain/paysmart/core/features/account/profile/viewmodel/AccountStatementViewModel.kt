package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.metalbrain.paysmart.core.features.account.profile.state.AccountStatementFormat
import net.metalbrain.paysmart.core.features.account.profile.state.AccountStatementUiState
import java.time.LocalDate
import java.util.Locale

@HiltViewModel
class AccountStatementViewModel @Inject constructor(
) : ViewModel() {

    private val formState = MutableStateFlow(
        AccountStatementUiState(
            selectedCurrencyCode = "GBP"
        )
    )

    val uiState: StateFlow<AccountStatementUiState> = formState.asStateFlow()

    fun onCurrencySelected(rawCurrencyCode: String) {
        val normalizedCurrencyCode = rawCurrencyCode.trim().uppercase(Locale.US)
        if (normalizedCurrencyCode.length != 3) return
        formState.update { currentState ->
            currentState.copy(selectedCurrencyCode = normalizedCurrencyCode)
        }
    }

    fun onStartDateSelected(date: LocalDate) {
        formState.update { currentState ->
            val adjustedEndDate = when {
                currentState.endDate == null -> null
                currentState.endDate.isBefore(date) -> date
                else -> currentState.endDate
            }
            currentState.copy(
                startDate = date,
                endDate = adjustedEndDate
            )
        }
    }

    fun onEndDateSelected(date: LocalDate) {
        formState.update { currentState ->
            val adjustedStartDate = when {
                currentState.startDate == null -> null
                currentState.startDate.isAfter(date) -> date
                else -> currentState.startDate
            }
            currentState.copy(
                startDate = adjustedStartDate,
                endDate = date
            )
        }
    }

    fun onFormatSelected(format: AccountStatementFormat) {
        formState.update { currentState ->
            currentState.copy(format = format)
        }
    }
}
