package net.metalbrain.paysmart.core.features.identity.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.identity.provider.captureLabel
import net.metalbrain.paysmart.core.features.identity.provider.frameShape
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun IdentityCaptureGuide(
    selectedDocument: KycDocumentType,
    isUploadSupported: Boolean
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    Text(
        text = stringResource(
            R.string.identity_resolver_capture_hint,
            selectedDocument.captureLabel,
            selectedDocument.frameShape.frameLabel()
        ),
        style = typography.bodySmall,
        color = colors.textSecondary
    )

    if (!selectedDocument.accepted) {
        Text(
            text = stringResource(R.string.identity_resolver_document_not_accepted),
            style = typography.bodySmall,
            color = colors.error
        )
        return
    }

    if (!isUploadSupported) {
        Text(
            text = stringResource(R.string.identity_resolver_document_not_supported),
            style = typography.bodySmall,
            color = colors.error
        )
    }
}
