package net.metalbrain.paysmart.ui.home


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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.domain.model.hasAddedHomeAddress
import net.metalbrain.paysmart.domain.model.hasVerifiedEmail
import net.metalbrain.paysmart.domain.model.hasVerifiedIdentity
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.CardDimensions
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileCompletionCardContent(
    user: AuthUserModel,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    viewModel: UserViewModel
) {
    val completed = listOf(
        user.hasVerifiedEmail,
        user.hasAddedHomeAddress,
        user.hasVerifiedIdentity
    ).count { it }

    val total = 3

    val progressPercent = (completed.toFloat() / total)
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = remember { mutableStateOf(false) }

    // 🔁 Animate sheet open
    LaunchedEffect(showSheet.value) {
        if (showSheet.value) {
            sheetState.show() // 👈 Smooth animated expansion
        } else {
            sheetState.hide()
        }
    }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState
        ) {
            // 🧩 This is your full account setup sheet
            AccountSetupSheetContent(
                user = (uiState as? UserUiState.ProfileLoaded)?.user ?: return@ModalBottomSheet,
                onAddAddressClick = { /* navigate to address */ },
                onVerifyIdentityClick = { /* navigate to identity */ }
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth()
        .padding(Dimens.smallScreenPadding),
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
                    progress = { progressPercent },
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(72.dp),
                    color = Color(0xFF00C853) // Green
                )
                Text(
                    text = "${progressPercent}\nCOMPLETED",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "Finish setting up your account.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Steps List
            ProfileStepItem("Verify your email", user.hasVerifiedEmail)
            ProfileStepItem("Add your home address", user.hasAddedHomeAddress)
            ProfileStepItem("Verify your identity", user.hasVerifiedIdentity)

            // CTA Button
            when {
                !user.hasVerifiedEmail -> {
                    PrimaryButton(
                        text = "Verify email",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onVerifyEmailClick
                    )
                }
                !user.hasAddedHomeAddress -> {
                    PrimaryButton(
                        text = "Add address",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAddAddressClick
                    )
                }

                !user.hasVerifiedIdentity -> {
                    PrimaryButton(
                        text = "Verify identity",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onVerifyIdentityClick
                    )
                }
                else -> {

                }
            }
        }
    }
}
