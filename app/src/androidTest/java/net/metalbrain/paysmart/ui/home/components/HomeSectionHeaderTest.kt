package net.metalbrain.paysmart.ui.home.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeSectionHeaderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun headerWithoutActionOnlyShowsTitle() {
        composeRule.setContent {
            PaysmartTheme {
                HomeSectionHeader(title = "Section title")
            }
        }

        composeRule.onNodeWithText("Section title").assertIsDisplayed()
        composeRule.onAllNodesWithText("See all").assertCountEquals(0)
    }
}
