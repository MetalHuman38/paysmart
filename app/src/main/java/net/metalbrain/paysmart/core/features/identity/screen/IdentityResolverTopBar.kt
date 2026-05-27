package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityResolverTopBar(onBack: () -> Unit) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.surfaceElevated,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.textPrimary
        ),
        title = {
            Text(
                text = stringResource(R.string.identity_resolver_title),
                style = typography.heading4
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back)
                )
            }
        }
    )
}
