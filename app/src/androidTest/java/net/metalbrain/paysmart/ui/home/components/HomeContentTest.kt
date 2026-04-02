package net.metalbrain.paysmart.ui.home.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.components.HomeContent
import net.metalbrain.paysmart.ui.home.state.HomeBalanceSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeExchangeRateSnapshot
import net.metalbrain.paysmart.ui.home.state.HomeNotificationUiState
import net.metalbrain.paysmart.ui.home.state.HomeTransactionProviderFilter
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersServicesAndAccountInformationSections() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                HomeContent(
                    onProfileClick = {},
                    onReferralClick = {},
                    onTransactionsClick = {},
                    onTransactionClick = { _: Transaction -> },
                    onCreateInvoiceClick = {},
                    onSendMoneyClick = {},
                    onRecentRecipientClick = {},
                    onReceiveMoneyClick = {},
                    onBalanceCardClick = {},
                    onRewardCardClick = {},
                    onAddMoneyClick = {},
                    onVerifyEmailClick = {},
                    onAddAddressClick = {},
                    onVerifyIdentityClick = {},
                    onViewRatesClick = {},
                    onViewAllLimitsClick = {},
                    localSettings = null,
                    displayName = "Test User",
                    transactions = emptyList<Transaction>(),
                    recentRecipients = emptyList(),
                    transactionSearchQuery = "",
                    isTransactionSearchActive = false,
                    availableTransactionProviders = emptyList<HomeTransactionProviderFilter>(),
                    selectedTransactionProviders = emptySet<HomeTransactionProviderFilter>(),
                    notification = HomeNotificationUiState(),
                    balanceSnapshot = HomeBalanceSnapshot(
                        balancesByCurrency = mapOf("GBP" to 1250.0),
                        preferredCurrencyCode = "GBP"
                    ),
                    rewardEarned = RewardEarnedSnapshot(points = 24.0),
                    countryIso2 = "GB",
                    countryFlagEmoji = "🇬🇧",
                    countryCurrencyCode = "GBP",
                    launchInterest = LaunchInterest.INVOICE,
                    capabilities = emptyList<CapabilityItem>(),
                    exchangeRateSnapshot = HomeExchangeRateSnapshot(
                        baseCurrencyCode = "GBP",
                        targetCurrencyCode = "USD",
                        rate = 1.25
                    ),
                    isBalanceVisible = true,
                    onTransactionSearchQueryChange = {},
                    onTransactionProviderToggle = { _: HomeTransactionProviderFilter -> },
                    onNotificationClick = {},
                    onToggleBalanceVisibility = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.home_services_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.home_account_information_title))
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun receiveMoneyCapabilityUsesDedicatedCallback() {
        var addMoneyClicks = 0
        var receiveMoneyClicks = 0

        composeRule.setContent {
            PaysmartTheme {
                HomeContent(
                    onProfileClick = {},
                    onReferralClick = {},
                    onTransactionsClick = {},
                    onTransactionClick = { _: Transaction -> },
                    onCreateInvoiceClick = {},
                    onSendMoneyClick = {},
                    onRecentRecipientClick = {},
                    onReceiveMoneyClick = { receiveMoneyClicks += 1 },
                    onBalanceCardClick = {},
                    onRewardCardClick = {},
                    onAddMoneyClick = { addMoneyClicks += 1 },
                    onVerifyEmailClick = {},
                    onAddAddressClick = {},
                    onVerifyIdentityClick = {},
                    onViewRatesClick = {},
                    onViewAllLimitsClick = {},
                    localSettings = LocalSecuritySettingsModel(
                        hasVerifiedEmail = true,
                        hasAddedHomeAddress = true
                    ),
                    displayName = "Test User",
                    transactions = emptyList<Transaction>(),
                    recentRecipients = emptyList(),
                    transactionSearchQuery = "",
                    isTransactionSearchActive = false,
                    availableTransactionProviders = emptyList<HomeTransactionProviderFilter>(),
                    selectedTransactionProviders = emptySet<HomeTransactionProviderFilter>(),
                    notification = HomeNotificationUiState(),
                    balanceSnapshot = HomeBalanceSnapshot(),
                    rewardEarned = RewardEarnedSnapshot(),
                    countryIso2 = "NG",
                    countryFlagEmoji = "🇳🇬",
                    countryCurrencyCode = "NGN",
                    launchInterest = LaunchInterest.TOP_UP,
                    capabilities = listOf(
                        CapabilityItem(
                            key = CapabilityKey.RECEIVE_MONEY,
                            title = "Receive money",
                            subtitle = "Get paid locally."
                        )
                    ),
                    exchangeRateSnapshot = HomeExchangeRateSnapshot(),
                    isBalanceVisible = true,
                    onTransactionSearchQueryChange = {},
                    onTransactionProviderToggle = { _: HomeTransactionProviderFilter -> },
                    onNotificationClick = {},
                    onToggleBalanceVisibility = {}
                )
            }
        }

        repeat(2) {
            composeRule.onRoot().performTouchInput { swipeUp() }
        }
        composeRule.onNodeWithText("Receive money").performClick()

        assertEquals(0, addMoneyClicks)
        assertEquals(1, receiveMoneyClicks)
    }
}
