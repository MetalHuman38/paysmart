package net.metalbrain.paysmart.ui.language

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Language
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.ScreenDimensions

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LanguageSelectionScreen(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var search by remember { mutableStateOf("") }

    val filteredLanguages = remember(search) {
        supportedLanguages.filter {
            it.name.contains(search, ignoreCase = true)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirectionFor(selectedLanguage)) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(top = Dimens.mediumSpacing)
                .padding(bottom = Dimens.mediumSpacing)
                .padding(horizontal = Dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(ScreenDimensions.smallSpacing)
        ) {
            // 🔙 Back
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .clickable { onContinue() }
                )
            }

            // Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.select_language),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.Black
                )
            }

            // Subtitle
            Text(
                text = stringResource(R.string.language_description),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Search
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // 🌍 Language list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLanguages) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = language.flagResId),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )

                        Spacer(Modifier.width(12.dp))

                        Text(
                            text = language.name,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp
                        )

                        AnimatedContent(
                            targetState = (language.code == selectedLanguage.code),
                            transitionSpec = {
                                (fadeIn(tween(200)) + scaleIn()).togetherWith(fadeOut(tween(100)))
                            },
                            label = "Checkmark Animation"
                        ) { isSelected ->
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF009E5D)
                                )
                            } else {
                                RadioButton(
                                    selected = false,
                                    onClick = { onLanguageSelected(language) }
                                )
                            }
                        }
                    }
                }
            }

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00B86B),
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(R.string.continue_text))
            }
        }
    }
}

fun layoutDirectionFor(language: Language): LayoutDirection {
    return if (language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}
