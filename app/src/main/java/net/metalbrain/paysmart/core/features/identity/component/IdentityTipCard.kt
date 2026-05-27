package net.metalbrain.paysmart.core.features.identity.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.identity.data.IdentityTipItem
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityTipCard(
    title: String,
    tips: List<IdentityTipItem>
) {
    val color = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PaysmartTheme.radius.medium,
        color = color.surfaceElevated,
        contentColor = color.textPrimary,
        tonalElevation = PaysmartTheme.elevation.subtle,
        border = BorderStroke(PaysmartTheme.border.thin, color.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = title,
                style = typography.heading4,
                color = color.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = color.fillHover,
                        contentColor = color.brandPrimary
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.minimumTouchTarget)
                                .padding(Dimens.sm),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tip.icon,
                                contentDescription = null,
                                tint = color.brandPrimary
                            )
                        }
                    }
                    Text(
                        text = tip.text,
                        modifier = Modifier.weight(1f),
                        style = typography.bodyMedium,
                        color = color.textSecondary
                    )
                }
            }
        }
    }
}
