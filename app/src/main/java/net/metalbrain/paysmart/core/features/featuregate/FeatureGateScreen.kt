package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureGateScreen(
    feature: FeatureKey,
    decision: FeatureGateDecision,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(feature.titleResId()))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(feature.descriptionResId()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    decision.requiredSecurityStrength?.let { requiredStrength ->
                        Text(
                            text = stringResource(
                                R.string.feature_gate_security_strength_progress,
                                decision.currentSecurityStrength,
                                requiredStrength
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    decision.missingRequirements.forEach { requirement ->
                        RequirementRow(requirement.toActionLabel())
                    }
                }
            }

            PrimaryButton(
                text = decision.nextRequirement?.toActionLabel() ?: "Continue",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RequirementRow(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(4.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun FeatureRequirement.toActionLabel(): String {
    return when (this) {
        FeatureRequirement.VERIFIED_EMAIL -> stringResource(R.string.profile_action_verify_email)
        FeatureRequirement.HOME_ADDRESS_VERIFIED -> stringResource(R.string.profile_action_complete_address)
        FeatureRequirement.IDENTITY_VERIFIED -> stringResource(R.string.profile_action_verify_identity)
        FeatureRequirement.SECURITY_STRENGTH_TWO ->
            stringResource(R.string.feature_gate_requirement_security_strength_two)
    }
}

private fun FeatureKey.titleResId(): Int {
    return when (this) {
        FeatureKey.ADD_MONEY -> R.string.feature_gate_add_money_title
        FeatureKey.SEND_MONEY -> R.string.feature_gate_send_money_title
        FeatureKey.CREATE_INVOICE -> R.string.feature_gate_invoice_title
    }
}

private fun FeatureKey.descriptionResId(): Int {
    return when (this) {
        FeatureKey.ADD_MONEY -> R.string.feature_gate_add_money_description
        FeatureKey.SEND_MONEY -> R.string.feature_gate_send_money_description
        FeatureKey.CREATE_INVOICE -> R.string.feature_gate_invoice_description
    }
}
