package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.LimitsRow
import net.metalbrain.paysmart.core.features.account.profile.util.resolveTierLabel
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountTierCard(
    settings: LocalSecuritySettingsModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {

                LimitsRow(
                    label = stringResource(R.string.account_limits_current_tier),
                    value = resolveTierLabel(settings)
                )

                HorizontalDivider()

                LimitsRow(
                    label = stringResource(R.string.account_limits_identity_status),
                    value = if (settings?.hasVerifiedIdentity == true) {
                        stringResource(R.string.common_verified)
                    } else {
                        stringResource(R.string.common_not_verified)
                    }
                )

                HorizontalDivider()

                LimitsRow(
                    label = stringResource(R.string.account_limits_address_status),
                    value = if (settings?.hasAddedHomeAddress == true) {
                        stringResource(R.string.common_completed)
                    } else {
                        stringResource(R.string.common_required)
                    }
                )

                HorizontalDivider()

                LimitsRow(
                    label = stringResource(R.string.account_limits_email_status),
                    value = if (settings?.hasVerifiedEmail == true) {
                        stringResource(R.string.common_verified)
                    } else {
                        stringResource(R.string.common_not_verified)
                    }
                )
            }
        }
    }
}
