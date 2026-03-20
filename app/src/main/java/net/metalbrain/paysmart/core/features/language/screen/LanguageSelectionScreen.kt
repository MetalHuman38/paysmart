package net.metalbrain.paysmart.core.features.language.screen


import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.features.language.data.layoutDirectionFor
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.language.component.LanguageSelectionContinueButton
import net.metalbrain.paysmart.core.features.language.component.LanguageSelectionList
import net.metalbrain.paysmart.core.features.language.component.LanguageSelectionSearchField

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
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
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.select_language),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = Dimens.xs
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
                    ) {
                        LanguageSelectionContinueButton(onClick = onContinue)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
                    .padding(bottom = Dimens.sm)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    LanguageSelectionDescription()

                    LanguageSelectionSearchField(
                        value = search,
                        onValueChange = { search = it }
                    )
                }

                LanguageSelectionList(
                    languages = filteredLanguages,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = onLanguageSelected,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = Dimens.md)
                )
            }

        }
    }
}
