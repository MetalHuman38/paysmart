package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.state.resolveStatus
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep

@Composable
fun IdentityVerificationPlanCard(state: IdentitySetupResolverUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.identity_resolver_plan_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IdentityStepLine(
                label = stringResource(R.string.identity_resolver_capture_step),
                status = state.resolveStatus(IdentityResolverStep.CAPTURE)
            )
            IdentityStepLine(
                label = stringResource(R.string.identity_resolver_encrypt_step),
                status = state.resolveStatus(IdentityResolverStep.ENCRYPT)
            )
            IdentityStepLine(
                label = stringResource(R.string.identity_resolver_upload_step),
                status = state.resolveStatus(IdentityResolverStep.UPLOAD)
            )
            IdentityStepLine(
                label = stringResource(R.string.identity_resolver_attest_step),
                status = state.resolveStatus(IdentityResolverStep.ATTEST)
            )
            IdentityStepLine(
                label = stringResource(R.string.identity_resolver_commit_step),
                status = state.resolveStatus(IdentityResolverStep.COMMIT)
            )
        }
    }
}
