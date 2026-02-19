package net.metalbrain.paysmart.ui.home.extensions


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.hasCompletedAddress
import net.metalbrain.paysmart.domain.model.hasCompletedEmailVerification
import net.metalbrain.paysmart.domain.model.hasCompletedIdentity
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.home.sheets.AccountSetupSheetContent
import net.metalbrain.paysmart.ui.theme.CardDimensions
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionCardContent(
    security: LocalSecuritySettingsModel,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {
    val completed = listOf(
        security.hasCompletedEmailVerification,
        security.hasCompletedAddress,
        security.hasCompletedIdentity
    ).count { it }

    val total = 3
    val progressPercent = completed.toFloat() / total
    val progressLabel = "${(progressPercent * 100).toInt()}%"

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = remember { mutableStateOf(false) }

    LaunchedEffect(showSheet.value) {
        if (showSheet.value) sheetState.show() else sheetState.hide()
    }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState
        ) {
            AccountSetupSheetContent(
                security = security,
                onAddAddressClick = { /* TODO: navigate to address */ },
                onVerifyIdentityClick = { /* TODO: navigate to identity */ }
            )
        }
    }

    // Header Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.smallScreenPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Get started",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        TextButton(onClick = { showSheet.value = true }) {
            Text("See all")
        }
    }

    // Card
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(CardDimensions.smallCardPadding),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular progress
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 6.dp,
                    color = Color(0xFF00C853)
                )
                Text(
                    text = progressLabel,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Finish setting up your account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            // Steps
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileStepItem("Verify your email", security.hasCompletedEmailVerification)
                ProfileStepItem("Add your home address", security.hasCompletedAddress)
                ProfileStepItem("Verify your identity", security.hasCompletedIdentity)
            }

            // CTA
            when {
                !security.hasCompletedEmailVerification -> {
                    PrimaryButton(
                        text = "Verify email",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onVerifyEmailClick
                    )
                }
                !security.hasCompletedAddress -> {
                    PrimaryButton(
                        text = "Add address",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAddAddressClick
                    )
                }
                !security.hasCompletedIdentity -> {
                    PrimaryButton(
                        text = "Verify identity",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onVerifyIdentityClick
                    )
                }
                else -> {
                    // All done!
                }
            }
        }
    }
}
