package net.metalbrain.paysmart.core.features.addmoney.card

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyErrorCode
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiError
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddMoneyErrorCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backendConfigurationIssueShowsCodeAndGuidance() {
        composeRule.setContent {
            PaysmartTheme {
                AddMoneyErrorCard(
                    error = AddMoneyUiError(
                        title = "Flutterwave emulator setup required",
                        message = "The local payments backend is missing Flutterwave provider credentials.",
                        code = AddMoneyErrorCode.MISSING_FLUTTERWAVE_SECRET_KEY,
                        supportingText = "Set FLUTTERWAVE_SECRET_KEY or FLUTTERWAVE_CLIENT_ID + FLUTTERWAVE_CLIENT_SECRET in the Functions env, then restart the backend.",
                        isConfigurationIssue = true
                    )
                )
            }
        }

        composeRule.onNodeWithText("Flutterwave emulator setup required").assertIsDisplayed()
        composeRule.onNodeWithText("Backend code: MISSING_FLUTTERWAVE_SECRET_KEY").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Set FLUTTERWAVE_SECRET_KEY or FLUTTERWAVE_CLIENT_ID + FLUTTERWAVE_CLIENT_SECRET in the Functions env, then restart the backend."
        ).assertIsDisplayed()
    }
}
