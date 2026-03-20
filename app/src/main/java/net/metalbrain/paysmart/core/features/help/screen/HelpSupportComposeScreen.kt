package net.metalbrain.paysmart.core.features.help.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.transactions.components.TransactionItem
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportComposeScreen(
    topic: HelpSupportTopic,
    selectedTransaction: Transaction?,
    onBack: () -> Unit,
    onSubmit: (message: String, attachmentUri: Uri?) -> Boolean
) {
    val context = LocalContext.current
    val topicLabel = stringResource(topic.listTitleRes ?: topic.categoryLabelRes)
    var message by rememberSaveable { mutableStateOf("") }
    var attachmentLabel by rememberSaveable { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }

    val attachmentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        attachmentUri = uri
        attachmentLabel = resolveAttachmentDisplayName(context, uri)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.help_compose_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = Dimens.xs
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
                ) {
                    Button(
                        onClick = {
                            if (onSubmit(message.trim(), attachmentUri)) {
                                onBack()
                            }
                        },
                        enabled = message.trim().isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.help_compose_submit))
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            selectedTransaction?.let { transaction ->
                TransactionItem(transaction = transaction)
            }

            Text(
                text = stringResource(
                    R.string.help_compose_category_label_format,
                    topicLabel
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(R.string.help_compose_prompt),
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp),
                placeholder = {
                    Text(text = stringResource(R.string.help_compose_message_placeholder))
                }
            )

            OutlinedButton(
                onClick = { attachmentLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.help_compose_attach_image))
            }

            if (attachmentLabel.isNotBlank()) {
                Text(
                    text = stringResource(
                        R.string.help_compose_attachment_selected_format,
                        attachmentLabel
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun resolveAttachmentDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
                ?.takeIf { value -> value.isNotBlank() }
                ?.let { value -> return value }
        }
    }
    return "attachment.jpg"
}
