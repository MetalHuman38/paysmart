package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.card.ActionCard
import net.metalbrain.paysmart.core.features.account.profile.card.ProfileInfoCard
import net.metalbrain.paysmart.core.features.account.profile.card.StatusCard
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileMissingItem
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    user: AuthUserModel,
    isLocked: Boolean,
    missingItems: List<ProfileMissingItem>,
    nextStep: ProfileNextStep?,
    onResolveSetup: () -> Unit,
    showSecuritySetupCta: Boolean = false,
    onContinueToSecuritySetup: (() -> Unit)? = null,
    showMfaNudgeCta: Boolean = false,
    onOpenMfaNudge: (() -> Unit)? = null,
    showPasskeyNudgeCta: Boolean = false,
    onOpenPasskeyNudge: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_details_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            if (isLocked) {
                StatusCard(
                    title = stringResource(R.string.profile_locked_title),
                    description = stringResource(R.string.profile_locked_message),
                    tone = ProfileCardTone.Critical
                )
            } else if (missingItems.isNotEmpty()) {
                ActionCard(
                    title = stringResource(R.string.profile_incomplete_title),
                    description = stringResource(R.string.profile_incomplete_description),
                    supportingItems = missingItems.map { stringResource(it.toLabelRes()) },
                    actionText = stringResource(nextStep.toActionRes()),
                    onAction = onResolveSetup,
                    tone = ProfileCardTone.Warning
                )
            }

            if (showSecuritySetupCta && missingItems.isEmpty() && onContinueToSecuritySetup != null) {
                ActionCard(
                    title = stringResource(R.string.profile_onboarding_ready_title),
                    description = stringResource(R.string.profile_onboarding_ready_description),
                    actionText = stringResource(R.string.profile_onboarding_continue_action),
                    onAction = onContinueToSecuritySetup,
                    tone = ProfileCardTone.Positive
                )
            }

            if (showMfaNudgeCta && onOpenMfaNudge != null) {
                ActionCard(
                    title = stringResource(R.string.profile_mfa_nudge_title),
                    description = stringResource(R.string.profile_mfa_nudge_description),
                    actionText = stringResource(R.string.profile_mfa_nudge_action),
                    onAction = onOpenMfaNudge
                )
            }

            if (showPasskeyNudgeCta && onOpenPasskeyNudge != null) {
                ActionCard(
                    title = stringResource(R.string.profile_passkey_nudge_title),
                    description = stringResource(R.string.profile_passkey_nudge_description),
                    actionText = stringResource(R.string.profile_passkey_nudge_action),
                    onAction = onOpenPasskeyNudge
                )
            }

            ProfileInfoCard(user = user)

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(
                    R.string.profile_version_format,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = Dimens.xs)
            )
        }
    }
}

private fun ProfileMissingItem.toLabelRes(): Int {
    return when (this) {
        ProfileMissingItem.FULL_NAME -> R.string.profile_field_full_name
        ProfileMissingItem.DATE_OF_BIRTH -> R.string.profile_field_date_of_birth
        ProfileMissingItem.ADDRESS_LINE_1 -> R.string.profile_field_address_line_1
        ProfileMissingItem.CITY -> R.string.profile_field_city
        ProfileMissingItem.EMAIL_ADDRESS -> R.string.profile_field_email
        ProfileMissingItem.PHONE_NUMBER -> R.string.profile_field_phone_number
        ProfileMissingItem.COUNTRY -> R.string.profile_field_country
        ProfileMissingItem.POSTAL_CODE -> R.string.profile_field_postal_code
        ProfileMissingItem.VERIFIED_EMAIL -> R.string.profile_missing_verified_email
        ProfileMissingItem.HOME_ADDRESS_VERIFIED -> R.string.profile_missing_home_address
        ProfileMissingItem.IDENTITY_VERIFIED -> R.string.profile_missing_identity
    }
}

private fun ProfileNextStep?.toActionRes(): Int {
    return when (this) {
        ProfileNextStep.VERIFY_EMAIL -> R.string.profile_action_verify_email
        ProfileNextStep.COMPLETE_ADDRESS -> R.string.profile_action_complete_address
        ProfileNextStep.VERIFY_IDENTITY -> R.string.profile_action_verify_identity
        ProfileNextStep.REVIEW_PROFILE -> R.string.profile_action_review_profile
        null -> R.string.profile_action_review_profile
    }
}
