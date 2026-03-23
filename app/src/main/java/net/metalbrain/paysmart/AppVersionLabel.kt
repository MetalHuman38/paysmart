package net.metalbrain.paysmart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AppVersionLabel(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            R.string.profile_version_format,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        ),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth()
    )
}
