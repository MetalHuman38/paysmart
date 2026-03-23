package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.card.AccountCreationHeroCard
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityProfile
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun PostOtpCapabilitiesContent(
    profile: CountryCapabilityProfile,
    selectedInterest: LaunchInterest,
    isPersistingSelection: Boolean,
    onInterestSelected: (LaunchInterest) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        AccountCreationHeroCard(
            emoji = profile.flagEmoji,
            title = stringResource(R.string.post_otp_capabilities_title),
            subtitle = stringResource(
                R.string.post_otp_capabilities_subtitle,
                profile.countryName
            )
        )

        LaunchInterestCard(
            title = stringResource(R.string.post_otp_interest_invoice_title),
            subtitle = stringResource(R.string.post_otp_interest_invoice_subtitle),
            footnote = stringResource(R.string.post_otp_interest_invoice_footnote),
            icon = Icons.Filled.Description,
            selected = selectedInterest == LaunchInterest.INVOICE,
            onClick = { onInterestSelected(LaunchInterest.INVOICE) }
        )

        LaunchInterestCard(
            title = stringResource(R.string.post_otp_interest_top_up_title),
            subtitle = stringResource(R.string.post_otp_interest_top_up_subtitle),
            footnote = CountryCapabilityCatalog.topUpPolicyHint(profile),
            icon = Icons.Filled.AddCard,
            selected = selectedInterest == LaunchInterest.TOP_UP,
            onClick = { onInterestSelected(LaunchInterest.TOP_UP) }
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Dimens.space10),
                verticalArrangement = Arrangement.spacedBy(Dimens.space4)
            ) {
                Text(
                    text = stringResource(R.string.post_otp_capabilities_currency_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = profile.currencyCode,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stringResource(R.string.post_otp_capabilities_supporting_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrimaryButton(
            text = stringResource(R.string.post_otp_capabilities_next),
            onClick = onNext,
            isLoading = isPersistingSelection,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.buttonHeight)
        )
    }
}

@Composable
private fun LaunchInterestCard(
    title: String,
    subtitle: String,
    footnote: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = Dimens.xs / 6,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.space10),
            verticalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = footnote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
