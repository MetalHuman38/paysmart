package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.animate.AnimatedLottieBackground
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun StartupContent(
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = Dimens.screenPadding)
    ) {
        StartupTopBar(
            currentLanguage = currentLanguage,
            onLanguageClick = onLanguageClick,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimens.largeSpacing)
                .padding(bottom = Dimens.largeSpacing),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StartupBrandSection(
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp, max = 320.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedLottieBackground()
            }

            StartupActionCard(
                onCreateAccountClick = onCreateAccountClick,
                onLoginClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
