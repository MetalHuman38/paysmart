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
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaySmartAppBackground

@Composable
fun StartupContent(
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    PaySmartAppBackground {
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
                    .fillMaxWidth()
                    .fillMaxSize()
                    .padding(top = Dimens.largeSpacing)
                    .padding(bottom = Dimens.largeSpacing),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StartupBrandSection(
                    modifier = Modifier.fillMaxWidth()
                )

                StartupAnimationStage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp, max = 320.dp)
                        .weight(1f)
                )

                StartupActionCard(
                    onCreateAccountClick = onCreateAccountClick,
                    onLoginClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
