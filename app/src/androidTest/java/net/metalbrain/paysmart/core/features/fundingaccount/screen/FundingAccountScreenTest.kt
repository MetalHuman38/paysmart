package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountStatus
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountScreenPhase
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FundingAccountScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun readyStateShowsCheckmarkAndAccountDetails() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                FundingAccountScreen(
                    state = FundingAccountUiState(
                        account = FundingAccountData(
                            accountId = "van_123",
                            provider = "flutterwave",
                            currency = "NGN",
                            accountNumber = "1234567890",
                            bankName = "Wema Bank",
                            accountName = "Ada Lovelace",
                            reference = "fb_ref_123",
                            status = FundingAccountStatus.ACTIVE,
                            providerStatus = "active",
                            customerId = "cus_123",
                            note = "Transfer to this account to fund your wallet.",
                            createdAtMs = 1_700_000_000_000,
                            updatedAtMs = 1_700_000_000_000
                        ),
                        phase = FundingAccountScreenPhase.READY,
                        isInitialLoading = false,
                        isMarketSupported = true,
                        countryName = "Nigeria",
                        countryFlagEmoji = "🇳🇬",
                        currencyCode = "NGN"
                    ),
                    onBack = {},
                    onRefresh = {},
                    onProvision = {},
                    onCopyAccountNumber = {},
                    onShareDetails = {}
                )
            }
        }

        composeRule.onNodeWithText(
            activity.getString(R.string.funding_account_state_ready_title)
        ).assertIsDisplayed()
        composeRule.onNodeWithText("1234 5678 90").assertIsDisplayed()
        composeRule.onNodeWithText(
            activity.getString(R.string.funding_account_action_copy_account_number)
        ).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(
            activity.getString(R.string.funding_account_status_ready_content_description)
        ).assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsProvisionAction() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                FundingAccountScreen(
                    state = FundingAccountUiState(
                        phase = FundingAccountScreenPhase.EMPTY,
                        isInitialLoading = false,
                        isMarketSupported = true,
                        countryName = "Nigeria",
                        countryFlagEmoji = "🇳🇬",
                        currencyCode = "NGN"
                    ),
                    onBack = {},
                    onRefresh = {},
                    onProvision = {},
                    onCopyAccountNumber = {},
                    onShareDetails = {}
                )
            }
        }

        composeRule.onNodeWithText(
            activity.getString(R.string.funding_account_action_provision)
        ).assertIsDisplayed()
    }
}
