package net.metalbrain.paysmart.ui.version

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.core.ui.R
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun AppVersionLabel(
    modifier: Modifier = Modifier,
    showAppName: Boolean = false,
) {
    val typography = PaysmartTheme.typographyTokens
    val colors = PaysmartTheme.colorTokens
    val spacing = PaysmartTheme.spacing
    val appVersionInfo = LocalAppVersionInfo.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.space1),
    ) {
        if (showAppName) {
            Text(
                text = stringResource(R.string.app_name),
                style = typography.labelSmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = stringResource(
                R.string.version_format,
                appVersionInfo.versionName,
                appVersionInfo.versionCode,
            ),
            style = typography.labelMedium,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
