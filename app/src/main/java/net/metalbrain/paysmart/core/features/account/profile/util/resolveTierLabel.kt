package net.metalbrain.paysmart.core.features.account.profile.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@Composable
fun resolveTierLabel(settings: LocalSecuritySettingsModel?): String {
    return when {
        settings?.hasVerifiedIdentity == true && settings.hasAddedHomeAddress == true ->
            stringResource(R.string.account_limits_tier_enhanced)

        settings?.hasVerifiedEmail == true ->
            stringResource(R.string.account_limits_tier_standard)

        else ->
            stringResource(R.string.account_limits_tier_restricted)
    }
}
