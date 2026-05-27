package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun FeatureGateStrengthCard(
    current: Int,
    required: Int
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    val progress = if (required <= 0) 0f else current.toFloat() / required.toFloat()

    Surface(
        shape = PaysmartTheme.radius.medium,
        color = colors.surfacePrimary,
        contentColor = colors.textPrimary,
        border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.feature_gate_requirement_security_strength_two),
                    modifier = Modifier.weight(1f),
                    style = typography.labelLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "$current / $required",
                    style = typography.labelLarge,
                    color = colors.brandPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = colors.brandPrimary,
                trackColor = colors.fillDisabled
            )
            Text(
                text = stringResource(R.string.feature_gate_security_strength_progress, current, required),
                style = typography.caption,
                color = colors.textSecondary
            )
        }
    }
}
