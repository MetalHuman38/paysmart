package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.animate.AnimatedLottieBackground
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.components.LanguageSelector
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun StartupScreen(
    navController: NavController,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    viewModel: LanguageViewModel,
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = Dimens.screenPadding)
    ) {
        // 🔹 Language Icon Button (top-right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.mediumSpacing)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(
                currentLanguage = currentLang,
                onClick = {
                    navController.navigate(Screen.Language.routeWithOrigin(Screen.Origin.STARTUP))
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = Dimens.largeSpacing)
                .padding(bottom = Dimens.largeSpacing),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 🔹 Spacer between language button and title
                Spacer(modifier = Modifier.height(Dimens.largeSpacing))

                // 🔹 App Title
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 🔹 Lottie Animation
            // 🔹 Animated Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedLottieBackground()
            }

            // 🔹 Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCreateAccountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(id = R.string.create_account))
                }

                Spacer(modifier = Modifier.height(Dimens.mediumSpacing))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeight),
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2C9C6A),
                        contentColor = Color.White
                    )
                ) {
                    Text(stringResource(id = R.string.log_in))
                }
            }
        }
    }
}

fun getLanguageDisplay(code: String): String = when (code) {
    "en" -> "English (United Kingdom)"
    "en-US" -> "English (United States)"
    "zh" -> "Chinese (中文)"
    "de" -> "Germany (Deutschland)"
    "fr" -> "French (Français)"
    "it" -> "Italian (Italiano)"
    "es" -> "Spanish (española)"
    "pt" -> "Portuguese (Português)"
    "ja" -> "Japan (日本)"
    "ko" -> "Korean (한국인)"
    else -> "Language"
}
