package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileDetailRow
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileMissingItem
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.domain.model.AuthUserModel

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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLocked) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_locked_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.profile_locked_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (missingItems.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_incomplete_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.profile_incomplete_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        missingItems.forEach { item ->
                            Text(
                                text = "\u2022 ${stringResource(item.toLabelRes())}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(
                            onClick = onResolveSetup,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(nextStep.toActionRes()))
                        }
                    }
                }
            }

            if (showSecuritySetupCta && missingItems.isEmpty() && onContinueToSecuritySetup != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_onboarding_ready_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.profile_onboarding_ready_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onContinueToSecuritySetup,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.profile_onboarding_continue_action))
                        }
                    }
                }
            }

            if (showMfaNudgeCta && onOpenMfaNudge != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_mfa_nudge_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.profile_mfa_nudge_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenMfaNudge,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.profile_mfa_nudge_action))
                        }
                    }
                }
            }

            if (showPasskeyNudgeCta && onOpenPasskeyNudge != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profile_passkey_nudge_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.profile_passkey_nudge_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenPasskeyNudge,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.profile_passkey_nudge_action))
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_full_name),
                        value = user.displayName
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_date_of_birth),
                        value = user.dateOfBirth
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_address_line_1),
                        value = user.addressLine1
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_address_line_2),
                        value = user.addressLine2
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_city),
                        value = user.city
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_email),
                        value = user.email
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_phone_number),
                        value = user.phoneNumber
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_country),
                        value = user.country
                    )
                    HorizontalDivider()
                    ProfileDetailRow(
                        label = stringResource(R.string.profile_field_postal_code),
                        value = user.postalCode
                    )
                }
            }

            Spacer(modifier =  Modifier.weight(1f))

            Text(
                text = stringResource(
                    R.string.profile_version_format,
                    BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE
                ),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
