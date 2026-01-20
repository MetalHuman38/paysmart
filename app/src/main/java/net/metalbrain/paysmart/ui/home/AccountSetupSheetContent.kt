package net.metalbrain.paysmart.ui.home

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.SecuritySettings
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.components.CircularProgressWithText
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SetupStep



@Composable
fun AccountSetupSheetContent(
    security: SecuritySettings,
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
    val subLabel = if (completedSteps == totalSteps) "ALL DONE" else "COMPLETED"

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
            text = "Complete your account setup",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Secure your account and do even more.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Steps list
        SetupStep(label = "Open a PaySmart account", done = true)
        SetupStep(label = "Verify your email", done = security.hasCompletedEmailVerification)
        SetupStep(label = "Add your home address", done = security.hasCompletedAddress, onClick = onAddAddressClick)
        SetupStep(label = "Verify your identity", done = security.hasCompletedIdentity, onClick = onVerifyIdentityClick)

        Spacer(modifier = Modifier.height(32.dp))

        // CTA — change based on status
        val buttonLabel = when {
            !security.hasCompletedAddress -> "Add address"
            !security.hasCompletedIdentity -> "Verify identity"
            else -> "Continue"
        }

        PrimaryButton(
            text = buttonLabel,
            onClick = {
                if (!security.hasCompletedAddress) onAddAddressClick()
                else if (!security.hasCompletedIdentity) onVerifyIdentityClick()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
