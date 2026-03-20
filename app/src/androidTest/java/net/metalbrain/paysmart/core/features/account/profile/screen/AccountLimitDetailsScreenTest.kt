package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitTabSpec
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitCardUiState
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitDetailsUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountLimitDetailsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun deepLinkedCurrencyScreenRendersTabsAndSwitchesCards() {
        composeRule.setContent {
            PaysmartTheme {
                var selectedTab by remember { mutableStateOf(AccountLimitKey.SEND) }
                val cards = if (selectedTab == AccountLimitKey.SEND) {
                    listOf(
                        AccountLimitCardUiState(
                            key = "single",
                            title = "Send Limit (Single transaction)",
                            progress = 0f,
                            leadingLabel = "£10,000.00",
                            trailingLabel = "£10,000.00"
                        )
                    )
                } else {
                    listOf(
                        AccountLimitCardUiState(
                            key = "single",
                            title = "Receive Limit (Single transaction)",
                            progress = 0f,
                            leadingLabel = "£8,000.00",
                            trailingLabel = "£8,000.00"
                        )
                    )
                }

                AccountLimitDetailsScreen(
                    state = AccountLimitDetailsUiState(
                        isLoading = false,
                        currencyCode = "GBP",
                        flagEmoji = "",
                        subtitle = "How much can you send and receive with your GBP PaySmart account",
                        tabs = listOf(
                            AccountLimitTabSpec(AccountLimitKey.SEND, "Limit on send"),
                            AccountLimitTabSpec(AccountLimitKey.RECEIVE, "Limit on receive")
                        ),
                        selectedTab = selectedTab,
                        cards = cards
                    ),
                    onBack = {},
                    onHelp = {},
                    onTabSelected = { selectedTab = it }
                )
            }
        }

        composeRule.onNodeWithText("GBP limits").assertIsDisplayed()
        composeRule.onNodeWithText("Send Limit (Single transaction)").assertIsDisplayed()
        composeRule.onNodeWithText("Limit on receive").performClick()
        composeRule.onNodeWithText("Receive Limit (Single transaction)").assertIsDisplayed()
    }
}
