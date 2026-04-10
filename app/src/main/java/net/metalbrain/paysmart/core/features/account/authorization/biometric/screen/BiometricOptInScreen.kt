package net.metalbrain.paysmart.core.features.account.authorization.biometric.screen




import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.BiometricToggleRow
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun BiometricOptInScreen(
    viewModel: BiometricOptInViewModel,
    activity: FragmentActivity,
    onSuccess: () -> Unit
) {
    // Check availability on first load
    LaunchedEffect(Unit) {
        viewModel.checkBiometricSupport(activity)
        return@LaunchedEffect
    }



    var userToggled by remember { mutableStateOf(false) }
    val biometricAvailable by viewModel.biometricAvailable.collectAsState()
    val biometricCompleted by viewModel.biometricCompleted.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val lockedOut by viewModel.lockedOut.collectAsState()

    val scrollState = rememberScrollState()

    val color = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens


    // Lottie Setup 🎞️
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.biometric))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LaunchedEffect(biometricCompleted, userToggled) {
        if (userToggled && biometricCompleted) {
            onSuccess()
        }
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding)
            .verticalScroll(scrollState), // ✅ Make scrollable
        horizontalAlignment = Alignment.CenterHorizontally

    )  {
        // 🔐 Lottie Animation
       Box(
           modifier = Modifier
               .size(160.dp),
           contentAlignment = Alignment.Center
       ){
           LottieAnimation(
               composition = composition,
               progress = { progress },
               modifier = Modifier
                   .size(160.dp),
               enableMergePaths = true,
               alignment = Alignment.Center
           )
       }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = stringResource(R.string.Secure_your_account),
            style = typography.heading4,
            fontWeight = FontWeight.Bold,
            color = color.textPrimary,
            textAlign = TextAlign.Center
        )


        Text(
            text = stringResource(R.string.add_passcode_title),
            style = typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = color.textSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 🌐 Biometric Toggle
        BiometricToggleRow(
            enabled = userToggled,
            onToggle = {
                if (!isLoading && biometricAvailable && !lockedOut) {
                    userToggled = true
                    viewModel.enableBiometric(
                        activity,
                        onSuccess = {
                            onSuccess()
                        }
                    )
                }
            },
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onSuccess) {
            Text(
                text = stringResource(R.string.mfa_prompt_skip_action),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (errorMsg != null) {
            Text(
                text = errorMsg ?: "",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_paysmart_logo),
                contentDescription = stringResource(R.string.content_desc_logo),
                modifier = Modifier.height(34.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
