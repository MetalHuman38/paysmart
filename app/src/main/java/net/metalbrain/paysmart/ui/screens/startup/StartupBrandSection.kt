package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
internal fun StartupBrandSection(
    modifier: Modifier = Modifier
) {
    val typography = PaysmartTheme.typographyTokens
    val color = PaysmartTheme.colorTokens

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = typography.heading1,
            color = color.textPrimary
        )
    }
}
