package net.metalbrain.paysmart.core.features.addmoney

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionStatus
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiState
import net.metalbrain.paysmart.core.features.addmoney.screen.AddMoneyActionSection
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMoneyActionSectionTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun flutterwaveAccountTransferSessionsUseAccountDetailsCallback() {
        val activity = composeRule.activity
        var providerCheckoutClicks = 0
        var accountDetailsClicks = 0

        renderActionSection(
            uiState = eligibleUiState(),
            onOpenProviderCheckout = { providerCheckoutClicks += 1 },
            onOpenAccountDetails = { accountDetailsClicks += 1 }
        )

        composeRule.onNodeWithText(activity.getString(R.string.add_money_view_account_details_action))
            .assertIsDisplayed()
            .performClick()

        assertEquals(0, providerCheckoutClicks)
        assertEquals(1, accountDetailsClicks)
    }

    @Test
    fun accountDetailsActionStaysHiddenForNonTransferSessions() {
        val accountDetailsLabel =
            composeRule.activity.getString(R.string.add_money_view_account_details_action)

        renderActionSection(uiState = eligibleUiState(activeSessionMethod = FxPaymentMethod.WIRE))

        composeRule.onAllNodesWithText(accountDetailsLabel).assertCountEquals(0)
    }

    @Test
    fun accountDetailsActionStaysHiddenForSettledTransferSessions() {
        val accountDetailsLabel =
            composeRule.activity.getString(R.string.add_money_view_account_details_action)

        renderActionSection(uiState = eligibleUiState(sessionStatus = AddMoneySessionStatus.SUCCEEDED))

        composeRule.onAllNodesWithText(accountDetailsLabel).assertCountEquals(0)
    }

    private fun eligibleUiState(
        activeSessionMethod: FxPaymentMethod = FxPaymentMethod.ACCOUNT_TRANSFER,
        sessionStatus: AddMoneySessionStatus = AddMoneySessionStatus.CREATED
    ): AddMoneyUiState {
        return AddMoneyUiState(
            sessionId = "flw_session_123",
            activeSessionProvider = AddMoneyProvider.FLUTTERWAVE,
            activeSessionMethod = activeSessionMethod,
            sessionStatus = sessionStatus
        )
    }

    private fun renderActionSection(
        uiState: AddMoneyUiState,
        activeProvider: AddMoneyProvider? = AddMoneyProvider.FLUTTERWAVE,
        onCreatePaymentSession: () -> Unit = {},
        onOpenProviderCheckout: () -> Unit = {},
        onOpenAccountDetails: () -> Unit = {},
        onRefreshSessionStatus: () -> Unit = {}
    ) {
        composeRule.setContent {
            PaysmartTheme {
                AddMoneyActionSection(
                    uiState = uiState,
                    activeProvider = activeProvider,
                    onCreatePaymentSession = onCreatePaymentSession,
                    onOpenProviderCheckout = onOpenProviderCheckout,
                    onOpenAccountDetails = onOpenAccountDetails,
                    onRefreshSessionStatus = onRefreshSessionStatus
                )
            }
        }
    }
}
