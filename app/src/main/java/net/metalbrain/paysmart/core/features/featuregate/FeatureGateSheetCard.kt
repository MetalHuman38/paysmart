package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun FeatureGateSheetCard(
    feature: FeatureKey,
    decision: FeatureGateDecision,
    modifier: Modifier = Modifier
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Surface(
        modifier = modifier,
        shape = PaysmartTheme.radius.large,
        color = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        tonalElevation = PaysmartTheme.elevation.card,
        shadowElevation = PaysmartTheme.elevation.subtle,
        border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Surface(
                shape = CircleShape,
                color = colors.fillHover,
                contentColor = colors.brandPrimary
            ) {
                Box(
                    modifier = Modifier.size(Dimens.minimumTouchTarget),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.brandPrimary
                    )
                }
            }

            Text(
                text = stringResource(R.string.feature_gate_security_check_title),
                style = typography.heading4,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )

            Text(
                text = stringResource(feature.descriptionResId()),
                style = typography.bodyMedium,
                color = colors.textSecondary
            )

            decision.requiredSecurityStrength?.let { requiredStrength ->
                FeatureGateStrengthCard(
                    current = decision.currentSecurityStrength,
                    required = requiredStrength
                )
            }

            if (decision.missingRequirements.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.space4)) {
                    decision.missingRequirements.forEach { requirement ->
                        FeatureGateRequirementRow(label = requirement.toActionLabel())
                    }
                }
            }
        }
    }
}
