package net.metalbrain.paysmart.ui.home.support

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeBalanceCurrencyResolverTest {

    @Test
    fun `returns preferred currency when balances are empty`() {
        val primaryCurrency = resolvePrimaryBalanceCurrency(
            balancesByCurrency = emptyMap(),
            preferredCurrencyCode = "ngn"
        )

        assertEquals("NGN", primaryCurrency)
    }

    @Test
    fun `matches preferred currency ignoring key casing`() {
        val balancesByCurrency = mapOf(
            "gbp" to 120.0,
            "ngn" to 48_000.0
        )

        val primaryCurrency = resolvePrimaryBalanceCurrency(
            balancesByCurrency = balancesByCurrency,
            preferredCurrencyCode = "NGN"
        )

        assertEquals("NGN", primaryCurrency)
        assertEquals(48_000.0, balancesByCurrency.balanceAmountForCurrency(primaryCurrency), 0.0)
    }

    @Test
    fun `falls back to deterministic currency when preferred currency is unavailable`() {
        val primaryCurrency = resolvePrimaryBalanceCurrency(
            balancesByCurrency = mapOf(
                "USD" to 25.0,
                "GBP" to 10.0
            ),
            preferredCurrencyCode = "NGN"
        )

        assertEquals("GBP", primaryCurrency)
    }
}
