package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentityDocumentSelectionBottomSheet(
    documents: List<KycDocumentType>,
    selectedDocumentId: String,
    onDismiss: () -> Unit,
    onDocumentSelected: (KycDocumentType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var search by remember { mutableStateOf("") }
    val query = search.trim()
    val filtered = remember(documents, query) {
        if (query.isBlank()) {
            documents
        } else {
            documents.filter { document ->
                document.formattedLabel.contains(query, ignoreCase = true) ||
                    document.id.contains(query, ignoreCase = true)
            }
        }
    }

    val acceptedDocuments = filtered.filter { it.accepted }
    val unavailableDocuments = filtered.filter { !it.accepted }
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.backgroundPrimary,
        contentColor = colors.textPrimary,
        shape = PaysmartTheme.radius.xLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md)
                .padding(bottom = Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.sheet_select_document_type_title),
                    style = typography.heading4,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = PaysmartTheme.radius.medium,
                placeholder = { Text(stringResource(R.string.sheet_search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.height(Dimens.md))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                if (acceptedDocuments.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.sheet_section_accepted),
                            style = typography.labelLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = Dimens.space2, bottom = Dimens.space2)
                        )
                    }

                    items(acceptedDocuments, key = { it.id }) { document ->
                        IdentityDocumentRow(
                            document = document,
                            selected = document.id == selectedDocumentId,
                            enabled = true,
                            onClick = {
                                onDocumentSelected(document)
                                onDismiss()
                            }
                        )
                    }
                }

                if (unavailableDocuments.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(Dimens.xs))
                        Text(
                            text = stringResource(R.string.sheet_section_not_accepted),
                            style = typography.labelLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = Dimens.space2, bottom = Dimens.space2)
                        )
                    }

                    items(unavailableDocuments, key = { it.id }) { document ->
                        IdentityDocumentRow(
                            document = document,
                            selected = document.id == selectedDocumentId,
                            enabled = false,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
