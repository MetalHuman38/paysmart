package net.metalbrain.paysmart.core.features.addmoney.screen

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
    fun flutterwaveAccountTransferSessionsUseReceiveMoneyCallback() {
        val activity = composeRule.activity
        var providerCheckoutClicks = 0
        var receiveMoneyClicks = 0

        composeRule.setContent {
            PaysmartTheme {
                AddMoneyActionSection(
                    uiState = eligibleUiState(),
                    activeProvider = AddMoneyProvider.FLUTTERWAVE,
                    onCreatePaymentSession = {},
                    onOpenProviderCheckout = { providerCheckoutClicks += 1 },
                    onOpenReceiveMoney = { receiveMoneyClicks += 1 },
                    onRefreshSessionStatus = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.funding_account_title))
            .assertIsDisplayed()
            .performClick()

        assertEquals(0, providerCheckoutClicks)
        assertEquals(1, receiveMoneyClicks)
    }

    @Test
    fun receiveMoneyActionStaysHiddenForNonTransferSessions() {
        val receiveMoneyLabel = composeRule.activity.getString(R.string.funding_account_title)

        composeRule.setContent {
            PaysmartTheme {
                AddMoneyActionSection(
                    uiState = eligibleUiState(activeSessionMethod = FxPaymentMethod.WIRE),
                    activeProvider = AddMoneyProvider.FLUTTERWAVE,
                    onCreatePaymentSession = {},
                    onOpenProviderCheckout = {},
                    onOpenReceiveMoney = {},
                    onRefreshSessionStatus = {}
                )
            }
        }

        composeRule.onAllNodesWithText(receiveMoneyLabel).assertCountEquals(0)
    }

    @Test
    fun receiveMoneyActionStaysHiddenForSettledTransferSessions() {
        val receiveMoneyLabel = composeRule.activity.getString(R.string.funding_account_title)

        composeRule.setContent {
            PaysmartTheme {
                AddMoneyActionSection(
                    uiState = eligibleUiState(sessionStatus = AddMoneySessionStatus.SUCCEEDED),
                    activeProvider = AddMoneyProvider.FLUTTERWAVE,
                    onCreatePaymentSession = {},
                    onOpenProviderCheckout = {},
                    onOpenReceiveMoney = {},
                    onRefreshSessionStatus = {}
                )
            }
        }

        composeRule.onAllNodesWithText(receiveMoneyLabel).assertCountEquals(0)
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
}
