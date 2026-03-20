package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitSelectorRowUiState
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitsListUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountLimitsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun walletDrivenRowsRenderFullCurrencyNamesAndDescriptors() {
        composeRule.setContent {
            PaysmartTheme {
                AccountLimitsScreen(
                    state = AccountLimitsListUiState(
                        isLoading = false,
                        accounts = listOf(
                            AccountLimitSelectorRowUiState(
                                currencyCode = "GBP",
                                currencyName = "Great British Pound",
                                accountDescriptor = "Account number, IBAN",
                                flagEmoji = "🇬🇧",
                                marketIso2 = "GB"
                            ),
                            AccountLimitSelectorRowUiState(
                                currencyCode = "KES",
                                currencyName = "Kenyan Shillings",
                                accountDescriptor = "Account number",
                                flagEmoji = "🇰🇪",
                                marketIso2 = "KE"
                            )
                        )
                    ),
                    settings = null,
                    onBack = {},
                    onAccountClick = { _ -> }
                )
            }
        }

        composeRule.onNodeWithText("Great British Pound").assertIsDisplayed()
        composeRule.onNodeWithText("Account number, IBAN").assertIsDisplayed()
        composeRule.onNodeWithText("Kenyan Shillings").assertIsDisplayed()
        composeRule.onNodeWithText("Account number").assertIsDisplayed()
    }
}
