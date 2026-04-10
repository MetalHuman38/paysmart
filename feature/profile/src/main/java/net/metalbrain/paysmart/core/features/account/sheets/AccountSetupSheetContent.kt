package net.metalbrain.paysmart.core.features.account.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.components.CircularProgressWithText
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SetupStep
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun AccountSetupSheetContent(
    security: LocalSecuritySettingsModel,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {
    LocalContext.current
    val totalSteps = 4
    val completedSteps = listOf(
        true, // Always completed: "Open a PaySmart account"
        security.hasCompletedEmailVerification,
        security.hasCompletedAddress,
        security.hasCompletedIdentity
    ).count { it }

    val progress = completedSteps.toFloat() / totalSteps
    val label = "$completedSteps/$totalSteps"
    val allDone = stringResource(R.string.all_done)
    val completed = stringResource(R.string.completed)
    val subLabel = if (completedSteps == totalSteps) allDone else completed
    val title = stringResource(R.string.complete_your_account_setup)
    val supporting = stringResource(R.string.secure_your_account_and_do_even_more)
    val openAccount = stringResource(R.string.open_a_paySmart_account)
    val verifyEmail = stringResource(R.string.verify_email)
    val addAddress = stringResource(R.string.add_address)
    val verifyIdentity = stringResource(R.string.verify_identity)
    val continueText = stringResource(R.string.continue_text)
    val color = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.lg, vertical = Dimens.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressWithText(
            progress = progress,
            label = label,
            subLabel = subLabel
        )

        Spacer(modifier = Modifier.height(Dimens.lg))

        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = typography.heading2,
            textAlign = TextAlign.Center,
        )

        Text(
            text = supporting,
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyMedium,
            color = color.textPrimary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(Dimens.lg))

        SetupStep(label = openAccount, done = true)
        SetupStep(label = verifyEmail, done = security.hasCompletedEmailVerification)
        SetupStep(
            label = addAddress,
            done = security.hasCompletedAddress,
            onClick = onAddAddressClick
        )
        SetupStep(
            label = verifyIdentity,
            done = security.hasCompletedIdentity,
            onClick = onVerifyIdentityClick
        )

        Spacer(modifier = Modifier.height(Dimens.xl))

        val buttonLabel = when {
            !security.hasCompletedAddress -> addAddress
            !security.hasCompletedIdentity -> verifyIdentity
            else -> continueText
        }

        PrimaryButton(
            text = buttonLabel,
            onClick = {
                if (!security.hasCompletedAddress) onAddAddressClick()
                else if (!security.hasCompletedIdentity) onVerifyIdentityClick()
            },
            contentColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
