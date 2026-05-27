package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun FeatureGateRequirementRow(label: String) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space6)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = colors.brandPrimary,
            modifier = Modifier
                .clip(CircleShape)
                .background(colors.fillHover)
                .padding(Dimens.space2)
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}
