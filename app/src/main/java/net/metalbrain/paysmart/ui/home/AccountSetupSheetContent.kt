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
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.hasAddedHomeAddress
import net.metalbrain.paysmart.domain.model.hasVerifiedEmail
import net.metalbrain.paysmart.domain.model.hasVerifiedIdentity
import net.metalbrain.paysmart.ui.components.CircularProgressWithText
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.SetupStep

@Composable
fun AccountSetupSheetContent(
    user: AuthUserModel,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress circle
        CircularProgressWithText(
            progress = 0.5f,
            label = "2/4",
            subLabel = "COMPLETED"
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

        // Steps list — checkbox and text
        SetupStep(
            label = "Open a PaySmart account",
            done = true
        )
        SetupStep(
            label = "Verify your email",
            done = user.hasVerifiedEmail
        )
        SetupStep(
            label = "Add your home address",
            done = user.hasAddedHomeAddress,
            onClick = onAddAddressClick
        )
        SetupStep(
            label = "Verify your identity",
            done = user.hasVerifiedIdentity,
            onClick = onVerifyIdentityClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Add address",
            onClick = onAddAddressClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
