package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.ui.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.language.data.resolveLanguageDisplaySpec
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun LanguageSelector(
    currentLanguage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language = resolveLanguageDisplaySpec(currentLanguage)
    val flagEmoji = CountrySelectionCatalog.flagForCountry(
        context = context,
        rawIso2 = language.countryIso2
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Dimens.minimumTouchTarget),
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = Dimens.md, vertical = Dimens.sm)
    ) {
        Text(
            text = flagEmoji,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(Dimens.sm))
        Text(
            text = stringResource(language.nameRes),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.width(Dimens.sm))
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = stringResource(R.string.content_desc_select_language),
            modifier = Modifier.size(20.dp)
        )
    }
}
