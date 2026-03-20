package net.metalbrain.paysmart.core.features.language.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun LanguageSelectionList(
    languages: List<Language>,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        items(languages, key = { language -> language.code }) { language ->
            LanguageSelectionRow(
                language = language,
                selected = language.code == selectedLanguage.code,
                onClick = { onLanguageSelected(language) }
            )
        }
    }
}
