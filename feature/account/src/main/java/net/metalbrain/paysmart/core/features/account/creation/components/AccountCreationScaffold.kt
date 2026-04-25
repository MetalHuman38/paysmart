package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCreationScaffold(
    onBack: () -> Unit,
    topBarAction: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val colors = PaysmartTheme.colorTokens

    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.backgroundGradientTop,
                    colors.backgroundGradientMiddle,
                    colors.surfaceElevated,
                    colors.backgroundGradientBottom
                )
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = colors.textPrimary,
                        actionIconContentColor = colors.textPrimary,
                        titleContentColor = colors.textPrimary
                    ),
                    title = { Text(text = "") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    actions = {
                        topBarAction?.invoke(this)
                    }
                )
            },
            content = content
        )
    }
}
