package net.metalbrain.paysmart.ui.home.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.screen.BalanceDetailsScreen
import net.metalbrain.paysmart.ui.home.state.BalanceDetailsUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BalanceDetailsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyTransactionsRenderDetailsSection() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                BalanceDetailsScreen(
                    state = BalanceDetailsUiState(
                        isLoading = false,
                        currencyCode = "GBP",
                        amount = 1250.0,
                        balancesByCurrency = mapOf("GBP" to 1250.0),
                        recentTransactions = emptyList()
                    ),
                    onBack = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.home_recent_activity_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.home_balance_no_transactions))
            .assertIsDisplayed()
    }
}
