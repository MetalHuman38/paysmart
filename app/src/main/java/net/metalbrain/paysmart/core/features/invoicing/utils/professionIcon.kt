package net.metalbrain.paysmart.core.features.invoicing.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.ui.graphics.vector.ImageVector

fun professionIcon(iconName: String): ImageVector {
    return when (iconName) {
        "medical_services" -> Icons.Filled.MedicalServices
        "shield" -> Icons.Filled.Security
        "work" -> Icons.Filled.WorkOutline
        "schedule" -> Icons.Filled.Schedule
        else -> Icons.Filled.Apps
    }
}
