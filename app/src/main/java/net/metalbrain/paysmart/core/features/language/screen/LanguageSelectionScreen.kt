package net.metalbrain.paysmart.core.features.language.screen


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import net.metalbrain.paysmart.core.features.language.data.layoutDirectionFor
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.ScreenDimensions

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LanguageSelectionScreen(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }
    val resources = LocalResources.current

    val filteredLanguages = supportedLanguages.filter { language ->
        resources.getString(language.nameRes).contains(search, ignoreCase = true) ||
            language.code.contains(search, ignoreCase = true)
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirectionFor(selectedLanguage)) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(top = Dimens.mediumSpacing)
                .padding(bottom = Dimens.mediumSpacing)
                .padding(horizontal = Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(ScreenDimensions.smallSpacing)
        ) {
            LanguageSelectionHeader(onBack = onBack)

            LanguageSelectionTitleBlock()

            LanguageSelectionSearchField(
                value = search,
                onValueChange = { search = it }
            )

            LanguageSelectionList(
                languages = filteredLanguages,
                selectedLanguage = selectedLanguage,
                onLanguageSelected = onLanguageSelected,
                modifier = Modifier.weight(1f)
            )

            LanguageSelectionContinueButton(onClick = onContinue)
        }
    }
}
