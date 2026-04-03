package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.metalbrain.paysmart.core.ui.R

@Composable
fun PhoneAlreadyRegisteredSheet(
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ❌ Close icon aligned top right
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.content_desc_close)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ⚠️ Warning icon (use custom drawable if you have one)
        Image(
            painter = painterResource(id = R.drawable.ic_warning),
            contentDescription = stringResource(R.string.content_desc_warning),
            modifier = Modifier
                .size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🧾 Title
        Text(
            text = stringResource(R.string.error),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 📢 Message
        Text(
            text = stringResource(R.string.phone_already_registered_message),
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ Dismiss button
        Button(
            onClick = onDismiss,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(R.string.dismiss),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
