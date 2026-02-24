package net.metalbrain.paysmart.ui.profile.identity.component

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun IdentityDocumentTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isSelected
    ) {
        Text(text)
    }
}
