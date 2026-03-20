package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.card.AccountCreationHeroCard
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun PostOtpCapabilitiesContent(
    profile: CountryCapabilityProfile,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        AccountCreationHeroCard(
            emoji = profile.flagEmoji,
            title = stringResource(
                R.string.post_otp_capabilities_title,
                profile.countryName
            ),
            subtitle = stringResource(R.string.post_otp_capabilities_subtitle)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimens.space10),
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                Text(
                    text = profile.currencyCode,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                profile.capabilities.forEach { capability ->
                    PostOtpCapabilityRow(
                        item = capability,
                        onClick = {}
                    )

                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.post_otp_capabilities_next),
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight)
        )
    }
}
