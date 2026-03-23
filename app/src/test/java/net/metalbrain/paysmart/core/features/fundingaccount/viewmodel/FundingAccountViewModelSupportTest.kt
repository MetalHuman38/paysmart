package net.metalbrain.paysmart.core.features.fundingaccount.viewmodel

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.capabilities.catalog.AddMoneyMarketPolicy
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FundingAccountViewModelSupportTest {

    @Test
    fun ukProfileDoesNotUseFlutterwaveFundingAccountFlow() {
        val profile = capabilityProfile(
            iso2 = "GB",
            providers = listOf(AddMoneyProvider.STRIPE),
            methods = listOf(
                FxPaymentMethod.DEBIT_CARD,
                FxPaymentMethod.CREDIT_CARD,
                FxPaymentMethod.ACCOUNT_TRANSFER
            ),
            includeReceiveMoney = true
        )

        assertFalse(isFundingAccountMarketSupported(profile))
    }

    @Test
    fun nigeriaProfileKeepsFlutterwaveFundingAccountFlow() {
        val profile = capabilityProfile(
            iso2 = "NG",
            providers = listOf(AddMoneyProvider.FLUTTERWAVE),
            methods = listOf(
                FxPaymentMethod.DEBIT_CARD,
                FxPaymentMethod.CREDIT_CARD,
                FxPaymentMethod.ACCOUNT_TRANSFER
            ),
            includeReceiveMoney = true
        )

        assertTrue(isFundingAccountMarketSupported(profile))
    }

    private fun capabilityProfile(
        iso2: String,
        providers: List<AddMoneyProvider>,
        methods: List<FxPaymentMethod>,
        includeReceiveMoney: Boolean
    ): CountryCapabilityProfile {
        val capabilities = buildList {
            add(
                CapabilityItem(
                    key = CapabilityKey.SEND_INTERNATIONAL,
                    title = "Send money abroad",
                    subtitle = "Cross-border transfer support."
                )
            )
            if (includeReceiveMoney) {
                add(
                    CapabilityItem(
                        key = CapabilityKey.RECEIVE_MONEY,
                        title = "Receive money",
                        subtitle = "Local account details where supported."
                    )
                )
            }
        }

        return CountryCapabilityProfile(
            iso2 = iso2,
            countryName = iso2,
            flagEmoji = "",
            currencyCode = if (iso2 == "GB") "GBP" else "NGN",
            addMoney = AddMoneyMarketPolicy(
                providers = providers,
                methods = methods
            ),
            capabilities = capabilities
        )
    }
}
