package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityDocumentRow(
    document: KycDocumentType,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = PaysmartTheme.radius.medium,
        color = if (selected && enabled) colors.fillHover else colors.surfaceElevated,
        contentColor = colors.textPrimary,
        border = BorderStroke(
            width = PaysmartTheme.border.thin,
            color = if (selected && enabled) {
                colors.brandPrimary.copy(alpha = 0.42f)
            } else {
                colors.borderSubtle
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = document.leadingIcon(),
                contentDescription = null,
                tint = if (enabled) {
                    colors.textSecondary
                } else {
                    colors.textTertiary
                }
            )

            Spacer(modifier = Modifier.width(Dimens.md))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                Text(
                    text = document.formattedLabel,
                    style = typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        colors.textPrimary
                    } else {
                        colors.textSecondary
                    }
                )
                if (!enabled) {
                    Text(
                        text = stringResource(R.string.sheet_not_accepted_inline),
                        style = typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.md))

            Icon(
                imageVector = if (selected) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = when {
                    selected && enabled -> colors.brandPrimary
                    selected -> colors.textTertiary
                    enabled -> colors.textTertiary
                    else -> colors.textTertiary.copy(alpha = 0.72f)
                }
            )
        }
    }
}

private fun KycDocumentType.leadingIcon(): ImageVector {
    return when (id.lowercase()) {
        "passport" -> Icons.Default.Description
        "drivers_license" -> Icons.Default.CreditCard
        else -> Icons.Default.CreditCard
    }
}
