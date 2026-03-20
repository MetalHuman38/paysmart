package net.metalbrain.paysmart.core.features.account.profile.viewmodel

import net.metalbrain.paysmart.core.features.account.profile.state.AccountStatementFormat
import net.metalbrain.paysmart.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AccountStatementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `default ui state uses csv format and default currency`() {
        val viewModel = AccountStatementViewModel()

        val state = viewModel.uiState.value

        assertEquals("GBP", state.selectedCurrencyCode)
        assertEquals(AccountStatementFormat.PDF, state.format)
    }
}
