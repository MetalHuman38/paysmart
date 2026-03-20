package net.metalbrain.paysmart.ui.home.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.screen.RewardDetailsScreen
import net.metalbrain.paysmart.ui.home.state.RewardDetailsUiState
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardDetailsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyRewardsHistoryShowsExpectedCopy() {
        val activity = composeRule.activity

        composeRule.setContent {
            PaysmartTheme {
                RewardDetailsScreen(
                    state = RewardDetailsUiState(
                        isLoading = false,
                        points = 42.0,
                        recentTransactions = emptyList()
                    ),
                    onBack = {},
                    onHelpClick = {}
                )
            }
        }

        composeRule.onNodeWithText(activity.getString(R.string.home_rewards_recent_transactions_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(activity.getString(R.string.home_rewards_no_recent_transactions))
            .assertIsDisplayed()
    }
}
