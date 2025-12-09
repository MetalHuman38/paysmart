package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.CardDimensions

@Composable
fun ProfileCompletionCardContent(
    profileState: ProfileSetupState,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {
    val progress = profileState.completedSteps / profileState.totalSteps.toFloat()

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(CardDimensions.smallCardPadding),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),

    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Circle
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(72.dp),
                    color = Color(0xFF00C853) // Green
                )
                Text(
                    text = "${profileState.completedSteps}/${profileState.totalSteps}\nCOMPLETED",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Complete your account setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Secure your account and do even more.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Steps List
            ProfileStepItem("Verify your email", profileState.hasVerifiedEmail)
            ProfileStepItem("Add your home address", profileState.hasAddedHomeAddress)
            ProfileStepItem("Verify your identity", profileState.hasVerifiedIdentity)

            // CTA Button
            when {
                !profileState.hasAddedHomeAddress -> {
                    PrimaryButton(text = "Add address", onClick = onAddAddressClick)
                }

                !profileState.hasVerifiedIdentity -> {
                    PrimaryButton(text = "Verify identity", onClick = onVerifyIdentityClick)
                }

                else -> {}
            }
        }
    }
}
