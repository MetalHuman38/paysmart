package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import net.metalbrain.paysmart.core.features.account.creation.card.PostOtpSecurityStepCard
import net.metalbrain.paysmart.core.features.account.creation.components.SecurityStepSpec
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test

class PostOtpSecurityStepCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersSecurityStepText() {
        composeRule.setContent {
            PaysmartTheme {
                PostOtpSecurityStepCard(
                    step = SecurityStepSpec(
                        icon = Icons.Filled.Shield,
                        title = "Set your local password",
                        description = "Used for account recovery and secure sign-in fallback."
                    )
                )
            }
        }

        composeRule.onNodeWithText("Set your local password").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Used for account recovery and secure sign-in fallback."
        ).assertIsDisplayed()
    }
}
