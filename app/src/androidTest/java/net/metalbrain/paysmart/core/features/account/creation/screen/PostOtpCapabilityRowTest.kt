package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import net.metalbrain.paysmart.core.features.account.creation.components.PostOtpCapabilityRow
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test

class PostOtpCapabilityRowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersCapabilityTitleSubtitleAndFootnote() {
        composeRule.setContent {
            PaysmartTheme {
                PostOtpCapabilityRow(
                    item = CapabilityItem(
                        key = CapabilityKey.EARN_RETURN,
                        title = "Earn a return",
                        subtitle = "Earn working-day returns on supported balances.",
                        footnote = "Rates for selected currencies."
                    )
                )
            }
        }

        composeRule.onNodeWithText("Earn a return").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Earn working-day returns on supported balances."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Rates for selected currencies.").assertIsDisplayed()
    }
}
