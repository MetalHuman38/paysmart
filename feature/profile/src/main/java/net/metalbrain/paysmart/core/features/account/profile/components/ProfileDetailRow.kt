package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun ProfileDetailRow(
    label: String,
    value: String?
) {
    val colors = PaysmartTheme.colorTokens
    val hasValue = !value.isNullOrBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.md, vertical = Dimens.md),
        horizontalArrangement = Arrangement.spacedBy(Dimens.md),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary,
            modifier = Modifier.weight(0.9f)
        )

        Text(
            text = value?.takeIf { it.isNotBlank() } ?: stringResource(R.string.profile_field_not_provided),
            style = MaterialTheme.typography.bodyLarge,
            color = if (hasValue) {
                colors.textPrimary
            } else {
                colors.textSecondary
            },
            modifier = Modifier.weight(1.1f)
        )
    }
}
