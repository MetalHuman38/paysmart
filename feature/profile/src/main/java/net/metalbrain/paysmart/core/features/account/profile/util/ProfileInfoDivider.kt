package net.metalbrain.paysmart.core.features.account.profile.util

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun ProfileInfoDivider() {
    HorizontalDivider(
        color = PaysmartTheme.colorTokens.borderSubtle.copy(alpha = 0.18f)
    )
}
