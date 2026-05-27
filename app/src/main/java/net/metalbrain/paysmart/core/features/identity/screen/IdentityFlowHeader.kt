package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun IdentityFlowHeader(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onHelp: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(Dimens.minimumTouchTarget)
                    .offset(x = -Dimens.space6)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = colors.textPrimary
                )
            }

            onHelp?.let { helpAction ->
                Surface(
                    modifier = Modifier.clickable(onClick = helpAction),
                    shape = PaysmartTheme.radius.pill,
                    color = colors.surfaceElevated,
                    contentColor = colors.brandPrimary,
                    border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
                ) {
                    Text(
                        text = stringResource(R.string.get_help),
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = typography.labelLarge,
                        color = colors.brandPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            Text(
                text = title,
                style = typography.heading2,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}
