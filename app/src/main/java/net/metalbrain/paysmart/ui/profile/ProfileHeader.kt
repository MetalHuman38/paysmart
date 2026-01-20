package net.metalbrain.paysmart.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ProfileHeader(
    displayName: String?,
    photoURL: String?,
    email: String?,
    isVerified: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (photoURL != null) {
            AsyncImage(
                model = photoURL,
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .padding(8.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(96.dp)
                    .padding(8.dp)
            )
        }

        Text(text = displayName ?: "PaySmart User", style = MaterialTheme.typography.titleMedium)
        Text(text = email ?: "No email", style = MaterialTheme.typography.bodyMedium)

        if (isVerified) {
            Text("✔️ Verified", style = MaterialTheme.typography.labelSmall)
        }
    }
}
