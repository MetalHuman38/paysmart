package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.utils.PasswordChecks

@Composable
fun RequirementsList(checks: PasswordChecks) {
    val iconSize = 18.dp
    val spacing = 8.dp
    val stylePassed = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.primary
    )
    val stylePending = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        PasswordRequirement("At least 12 characters", checks.lengthOK, iconSize, spacing, stylePassed, stylePending)
        PasswordRequirement("Uppercase letter", checks.upperOK, iconSize, spacing, stylePassed, stylePending)
        PasswordRequirement("Lowercase letter", checks.lowerOK, iconSize, spacing, stylePassed, stylePending)
        PasswordRequirement("Number", checks.digitOK, iconSize, spacing, stylePassed, stylePending)
        PasswordRequirement("Symbol (e.g. !@#$%)", checks.symbolOK, iconSize, spacing, stylePassed, stylePending)
    }
}

@Composable
private fun PasswordRequirement(
    label: String,
    passed: Boolean,
    iconSize: Dp,
    spacing: Dp,
    passedStyle: TextStyle,
    pendingStyle: TextStyle
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(spacing))
        Text(
            text = label,
            style = if (passed) passedStyle else pendingStyle
        )
    }
}
