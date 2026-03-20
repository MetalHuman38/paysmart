package net.metalbrain.paysmart.core.features.language.screen


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R

@Composable
fun LanguageSelectionDescription() {
    Text(
        text = stringResource(R.string.language_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
