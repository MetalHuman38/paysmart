package net.metalbrain.paysmart.core.features.featuregate

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R

@Composable
fun FeatureRequirement.toActionLabel(): String {
    return when (this) {
        FeatureRequirement.VERIFIED_EMAIL -> stringResource(R.string.profile_action_verify_email)
        FeatureRequirement.HOME_ADDRESS_VERIFIED -> stringResource(R.string.profile_action_complete_address)
        FeatureRequirement.IDENTITY_VERIFIED -> stringResource(R.string.profile_action_verify_identity)
        FeatureRequirement.SECURITY_STRENGTH_TWO ->
            stringResource(R.string.feature_gate_requirement_security_strength_two)
    }
}

fun FeatureKey.titleResId(): Int {
    return when (this) {
        FeatureKey.ADD_MONEY -> R.string.feature_gate_add_money_title
        FeatureKey.RECEIVE_MONEY -> R.string.feature_gate_receive_money_title
        FeatureKey.SEND_MONEY -> R.string.feature_gate_send_money_title
        FeatureKey.CREATE_INVOICE -> R.string.feature_gate_invoice_title
    }
}

fun FeatureKey.descriptionResId(): Int {
    return when (this) {
        FeatureKey.ADD_MONEY -> R.string.feature_gate_add_money_description
        FeatureKey.RECEIVE_MONEY -> R.string.feature_gate_receive_money_description
        FeatureKey.SEND_MONEY -> R.string.feature_gate_send_money_description
        FeatureKey.CREATE_INVOICE -> R.string.feature_gate_invoice_description
    }
}
