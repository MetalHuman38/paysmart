package net.metalbrain.paysmart.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.language.data.resolveLanguageDisplaySpec
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LanguageSelectorTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun unknownLanguageUsesCatalogDefaultFlagAndLabel() {
        val activity = composeRule.activity
        val expectedFlag = CountrySelectionCatalog.flagForCountry(
            context = activity,
            rawIso2 = CountryCapabilityCatalog.defaultProfile().iso2
        )
        val expectedLabel = activity.getString(resolveLanguageDisplaySpec("unknown").nameRes)

        composeRule.setContent {
            PaysmartTheme {
                LanguageSelector(
                    currentLanguage = "unknown",
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithText(expectedFlag).assertIsDisplayed()
        composeRule.onNodeWithText(expectedLabel).assertIsDisplayed()
    }
}
