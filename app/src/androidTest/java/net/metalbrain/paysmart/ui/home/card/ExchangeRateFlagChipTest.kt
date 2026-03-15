package net.metalbrain.paysmart.ui.home.card

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExchangeRateFlagChipTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersBothFlags() {
        composeRule.setContent {
            PaysmartTheme {
                ExchangeRateFlagChip(
                    baseFlag = "GBP",
                    targetFlag = "USD"
                )
            }
        }

        composeRule.onNodeWithText("GBP").assertIsDisplayed()
        composeRule.onNodeWithText("USD").assertIsDisplayed()
    }
}
