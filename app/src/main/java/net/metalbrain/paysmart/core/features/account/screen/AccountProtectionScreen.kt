package net.metalbrain.paysmart.core.features.account.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun AccountProtectionContent(
    onSetPasscodeClick: () -> Unit,
    onSetBiometricClick: () -> Unit,
    onSetPasskeyClick: (() -> Unit)? = null,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock image or icon
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lock))
            val progress by animateLottieCompositionAsState(
                composition,
                iterations = LottieConstants.IterateForever,
                isPlaying = true
            )

            Column(
                modifier = Modifier.padding(vertical = 22.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .size(160.dp),
                    enableMergePaths = true
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Title
            Text(
                text = stringResource(R.string.Secure_your_account),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = stringResource(R.string.set_up_biometric),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = stringResource(R.string.set_up_biometric),
                onClick = onSetBiometricClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSetPasscodeClick) {
                Text(text = stringResource(R.string.set_up_passcode))
            }

            if (onSetPasskeyClick != null) {
                TextButton(onClick = onSetPasskeyClick) {
                    Text(text = stringResource(R.string.continue_with_passkey))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advisory Subtext
            Text(
                text = stringResource(R.string.add_passcode_advisory_text),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

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
}
