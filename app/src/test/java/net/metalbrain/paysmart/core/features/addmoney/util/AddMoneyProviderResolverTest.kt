package net.metalbrain.paysmart.core.features.addmoney.util

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddMoneyProviderResolverTest {

    @Test
    fun `preferred provider uses first allowed provider`() {
        assertEquals(
            AddMoneyProvider.FLUTTERWAVE,
            resolvePreferredAddMoneyProvider(
                listOf(AddMoneyProvider.FLUTTERWAVE, AddMoneyProvider.STRIPE)
            )
        )
    }

    @Test
    fun `preferred provider is null when catalog has no enabled providers`() {
        assertNull(resolvePreferredAddMoneyProvider(emptyList()))
    }
}
