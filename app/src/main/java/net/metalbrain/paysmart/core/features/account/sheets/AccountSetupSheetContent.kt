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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.components.CircularProgressWithText
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SetupStep
import net.metalbrain.paysmart.R




@Composable
fun AccountSetupSheetContent(
    security: LocalSecuritySettingsModel,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {
    val totalSteps = 4
    val completedSteps = listOf(
        true, // Always completed: "Open a PaySmart account"
        security.hasCompletedEmailVerification,
        security.hasCompletedAddress,
        security.hasCompletedIdentity
    ).count { it }

    val progress = completedSteps.toFloat() / totalSteps
    val label = "$completedSteps/$totalSteps"
    val allDone = stringResource(id = R.string.all_done)
    val completed = stringResource(id = R.string.completed)
    val subLabel = if (completedSteps == totalSteps) allDone else completed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress circle — center-aligned
        CircularProgressWithText(
            progress = progress,
            label = label,
            subLabel = subLabel
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.complete_your_account_setup),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(id = R.string.secure_your_account_and_do_even_more),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps list
        SetupStep(label = stringResource(id = R.string.open_a_paySmart_account), done = true)
        SetupStep(label = stringResource(id = R.string.verify_email), done = security.hasCompletedEmailVerification)
        SetupStep(label = stringResource(id = R.string.add_address), done = security.hasCompletedAddress, onClick = onAddAddressClick)
        SetupStep(label = stringResource(id = R.string.verify_identity), done = security.hasCompletedIdentity, onClick = onVerifyIdentityClick)

        Spacer(modifier = Modifier.height(32.dp))

        // CTA — change based on status
        val buttonLabel = when {
            !security.hasCompletedAddress -> stringResource(id = R.string.add_address)
            !security.hasCompletedIdentity -> stringResource(id = R.string.verify_identity)
            else -> "Continue"
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
