package net.metalbrain.paysmart.ui.profile.identity.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.ui.profile.identity.provider.captureLabel
import net.metalbrain.paysmart.ui.profile.identity.provider.frameShape


@Composable
fun IdentityCaptureGuide(
    selectedDocument: KycDocumentType,
    isUploadSupported: Boolean
) {
    Text(
        text = stringResource(
            R.string.identity_resolver_capture_hint,
            selectedDocument.captureLabel,
            selectedDocument.frameShape.frameLabel()
        ),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (!selectedDocument.accepted) {
        Text(
            text = stringResource(R.string.identity_resolver_document_not_accepted),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    if (!isUploadSupported) {
        Text(
            text = stringResource(R.string.identity_resolver_document_not_supported),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
