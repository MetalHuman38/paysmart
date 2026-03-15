package net.metalbrain.paysmart.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoConnectionGateScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun offlineStateShowsNetworkCopy() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                NoConnectionGateScreen(
                    isOnline = false,
                    onReturnToLogin = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.no_connection)).assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.no_internet_connection))
            .assertIsDisplayed()
    }
}
