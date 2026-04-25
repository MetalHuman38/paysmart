package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.domain.model.countryDisplayName
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun CountryHeaderRow(
    country: Country,
    onClick: () -> Unit
) {
    val colors = PaysmartTheme.colorTokens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.select_country),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = countryDisplayName(country),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.content_desc_select_country),
                        tint = colors.textSecondary
                    )
                }
            }
        }
    }
}
