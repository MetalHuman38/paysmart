package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.card.AccountStatementMessageCard
import net.metalbrain.paysmart.core.features.account.profile.card.AccountStatementTransactionCard
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountStatementScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyPagingDataShowsEmptyState() {
        val emptyLabel = composeRule.activity.getString(R.string.account_statement_empty)

        composeRule.setContent {
            PaysmartTheme {
                AccountStatementMessageCard(
                    message = emptyLabel
                )
            }
        }

        composeRule.onNodeWithText(emptyLabel).assertIsDisplayed()
    }

    @Test
    fun pagedTransactionsRenderStatementRows() {
        val statusLabel = composeRule.activity.getString(
            R.string.account_statement_status_format,
            "Successful"
        )

        composeRule.setContent {
            PaysmartTheme {
                AccountStatementTransactionCard(
                    transaction = Transaction(
                        id = "tx_statement",
                        title = "Top up via Stripe",
                        amount = 25.0,
                        currency = "GBP",
                        status = "Successful",
                        iconRes = 0,
                        createdAtMs = 1_700_000_000_000
                    )
                )
            }
        }

        composeRule.onNodeWithText("Top up via Stripe").assertIsDisplayed()
        composeRule.onNodeWithText(statusLabel).assertIsDisplayed()
    }
}
