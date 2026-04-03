package net.metalbrain.paysmart.ui.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.home.R
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@Composable
fun dailyLimitsHint(localSettings: LocalSecuritySettingsModel?): String {
    return when {
        localSettings?.hasVerifiedIdentity == true ->
            stringResource(R.string.home_daily_limits_hint_verified)

        localSettings?.hasAddedHomeAddress == true && localSettings.hasVerifiedEmail ->
            stringResource(R.string.home_daily_limits_hint_identity_required)

        localSettings?.hasVerifiedEmail == true ->
            stringResource(R.string.home_daily_limits_hint_address_required)

        else -> stringResource(R.string.home_daily_limits_hint_email_required)
    }
}
