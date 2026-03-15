package net.metalbrain.paysmart.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.screens.startup.StartupContent
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun startupContentRendersPrimaryActions() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                StartupContent(
                    currentLanguage = "en",
                    onLanguageClick = {},
                    onCreateAccountClick = {},
                    onLoginClick = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.create_account)).assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.log_in)).assertIsDisplayed()
    }
}
