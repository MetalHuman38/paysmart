package net.metalbrain.paysmart.core.features.account.authentication.login.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.LanguageSelector


@Composable
fun LoginHeaderRow(
    currentLanguage: String,
    onBackClicked: () -> Unit,
    onLanguageClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.common_back)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        LanguageSelector(
            currentLanguage = currentLanguage,
            onClick = onLanguageClick
        )
    }
}
