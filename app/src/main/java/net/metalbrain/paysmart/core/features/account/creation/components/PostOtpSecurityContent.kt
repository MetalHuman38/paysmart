package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.creation.card.AccountCreationHeroCard
import net.metalbrain.paysmart.core.features.account.creation.card.PostOtpSecurityStepCard
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun PostOtpSecurityContent(
    countryName: String,
    flagEmoji: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Dimens.space8)
    ) {
        AccountCreationHeroCard(
            emoji = flagEmoji,
            title = stringResource(R.string.post_otp_security_title),
            subtitle = stringResource(R.string.post_otp_security_subtitle, countryName)
        )

        SecurityStepSection(
            title = stringResource(R.string.post_otp_security_required_title),
            steps = listOf(
                SecurityStepSpec(
                    icon = Icons.Filled.Person,
                    title = stringResource(R.string.post_otp_security_step_legal_title),
                    description = stringResource(R.string.post_otp_security_step_legal_description)
                ),
                SecurityStepSpec(
                    icon = Icons.Filled.Shield,
                    title = stringResource(R.string.post_otp_security_step_password_title),
                    description = stringResource(R.string.post_otp_security_step_password_description)
                )
            )
        )

        SecurityStepSection(
            title = stringResource(R.string.post_otp_security_optional_title),
            steps = listOf(
                SecurityStepSpec(
                    icon = Icons.Filled.Fingerprint,
                    title = stringResource(R.string.post_otp_security_step_biometric_title),
                    description = stringResource(R.string.post_otp_security_step_biometric_description)
                ),
                SecurityStepSpec(
                    icon = Icons.Filled.Email,
                    title = stringResource(R.string.post_otp_security_step_email_title),
                    description = stringResource(R.string.post_otp_security_step_email_description)
                )
            )
        )

        PrimaryButton(
            text = stringResource(R.string.continue_text),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SecurityStepSection(
    title: String,
    steps: List<SecurityStepSpec>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimens.space10),
            verticalArrangement = Arrangement.spacedBy(Dimens.space8)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            steps.forEach { step ->
                PostOtpSecurityStepCard(step = step)
            }
        }
    }
}

internal data class SecurityStepSpec(
    val icon: ImageVector,
    val title: String,
    val description: String
)
