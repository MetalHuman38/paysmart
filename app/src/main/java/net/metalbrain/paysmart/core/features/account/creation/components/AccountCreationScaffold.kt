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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AccountCreationScaffold(
    onBack: () -> Unit,
    topBarAction: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val backgroundPalette = LocalAppThemePack.current.darkBackground

    Box(
        modifier = Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundPalette.start,
                    backgroundPalette.accentOne,
                    backgroundPalette.accentTwo,
                    backgroundPalette.end
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
                        navigationIconContentColor = Color.Unspecified,
                        actionIconContentColor = Color.Unspecified,
                        titleContentColor = Color.Unspecified
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
