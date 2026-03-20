package net.metalbrain.paysmart.core.features.language.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.language.data.resolveLanguageDisplaySpec
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.ui.theme.Dimens
import kotlin.text.uppercase


@Composable
fun LanguageSelectionRow(
    language: Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val display = resolveLanguageDisplaySpec(language.code)
    val flagEmoji = CountrySelectionCatalog.flagForCountry(
        context = context,
        rawIso2 = display.countryIso2
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (selected) Dimens.xs else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactLanguageFlag(flagEmoji = flagEmoji)

            Text(
                text = stringResource(id = display.nameRes),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = language.code.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.content_desc_selected),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
