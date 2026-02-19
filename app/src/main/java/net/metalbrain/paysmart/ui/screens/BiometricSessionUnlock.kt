package net.metalbrain.paysmart.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.BiometricOptInViewModel

@Composable
fun BiometricSessionUnlock(
    onUnlock: () -> Unit,
) {

    val viewModel: BiometricOptInViewModel = hiltViewModel()
    val activity = LocalActivity.current as FragmentActivity

    // Check availability on first load
    LaunchedEffect(Unit) {
        viewModel.checkBiometricSupport(activity)
        return@LaunchedEffect
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.face_verification))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
    
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(160.dp)) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(160.dp),
                enableMergePaths = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Unlock your session",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Use your fingerprint or face to continue",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            enabled = !isLoading,
            text = if (isLoading) "Authenticating..." else "Unlock",
            onClick = { 
                viewModel.authenticateBiometric(
                    activity = activity,
                    onSuccess = onUnlock
                )
            },
            modifier = Modifier,
        )
    }
}
