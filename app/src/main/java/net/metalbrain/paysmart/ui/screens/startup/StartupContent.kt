package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.screens.PaySmartScreen
import net.metalbrain.paysmart.ui.screens.ScreenSpacing

@Composable
fun StartupContent(
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    PaySmartScreen(
        topBar = {
            StartupTopBar(
                currentLanguage = currentLanguage,
                onLanguageClick = onLanguageClick
            )
        },
        bottomBar = {
            StartupActionCard(
                onCreateAccountClick = onCreateAccountClick,
                onLoginClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = ScreenSpacing.contentHorizontal,
                        end = ScreenSpacing.contentHorizontal,
                        bottom = ScreenSpacing.contentVertical
                    )
            )
        }
    ) {
        Spacer(modifier = Modifier.height(ScreenSpacing.sectionGap))

        StartupBrandSection(
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(ScreenSpacing.sectionGap))

        StartupAnimationStage(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 230.dp, max = 310.dp)
                .weight(1f, fill = false)
        )
    }
}
