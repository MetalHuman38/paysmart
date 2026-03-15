package net.metalbrain.paysmart.core.features.addmoney.card

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class AddMoneyAvailabilityCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersUnavailableMarketCopy() {
        composeRule.setContent {
            AddMoneyAvailabilityCard(countryName = "Japan")
        }

        composeRule.onNodeWithText("Add money unavailable").assertIsDisplayed()
        composeRule.onNodeWithText("Add money is not available in Japan yet.")
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            "We will enable top ups in Japan once a supported funding provider is live for that market."
        ).assertIsDisplayed()
    }
}
