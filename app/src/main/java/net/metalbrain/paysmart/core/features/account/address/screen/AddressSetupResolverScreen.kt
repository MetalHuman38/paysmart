package net.metalbrain.paysmart.core.features.account.address.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.address.viewmodel.AddressSetupResolverStep
import net.metalbrain.paysmart.core.features.account.address.viewmodel.AddressSetupResolverViewModel
import net.metalbrain.paysmart.core.features.account.address.component.AddressLookupStep

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressSetupResolverScreen(
    viewModel: AddressSetupResolverViewModel,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val titleRes = when (state.step) {
        AddressSetupResolverStep.INPUT -> R.string.address_resolver_title
        AddressSetupResolverStep.MAP_CONFIRM -> R.string.address_resolver_map_title
        AddressSetupResolverStep.FINAL_CONFIRM -> R.string.address_resolver_final_title
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when (state.step) {
                                AddressSetupResolverStep.INPUT -> onBack()
                                AddressSetupResolverStep.MAP_CONFIRM -> viewModel.backToInput()
                                AddressSetupResolverStep.FINAL_CONFIRM -> viewModel.backToMapConfirm()
                            }
                        }
                    ) {
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (state.step) {
                AddressSetupResolverStep.INPUT -> {
                    AddressLookupStep(
                        house = state.house,
                        postcode = state.postcode,
                        country = state.country,
                        isLoading = state.isLoading,
                        onHouseChanged = viewModel::onHouseChanged,
                        onPostcodeChanged = viewModel::onPostcodeChanged,
                        onCountryChanged = viewModel::onCountryChanged,
                        onResolve = viewModel::resolveAddress
                    )
                }

                AddressSetupResolverStep.MAP_CONFIRM -> {
                    val resolved = state.resolvedAddress
                    if (resolved != null) {
                        AddressMapConfirmStep(
                            fullAddress = resolved.fullAddressWithHouse.ifBlank { resolved.fullAddress },
                            line1 = resolved.line1,
                            city = resolved.city,
                            postCode = resolved.postCode,
                            countryCode = resolved.countryCode,
                            source = resolved.source,
                            lat = resolved.lat,
                            lng = resolved.lng,
                            onNoClick = viewModel::backToInput,
                            onYesClick = viewModel::goToFinalConfirmation
                        )
                    }
                }

                AddressSetupResolverStep.FINAL_CONFIRM -> {
                    AddressFinalConfirmStep(
                        line1 = state.line1Draft,
                        line2 = state.line2Draft,
                        city = state.cityDraft,
                        stateOrRegion = state.stateOrRegionDraft,
                        postCode = state.postCodeDraft,
                        countryCode = state.countryCodeDraft,
                        isSaving = state.isSaving,
                        onLine1Changed = viewModel::onLine1DraftChanged,
                        onLine2Changed = viewModel::onLine2DraftChanged,
                        onCityChanged = viewModel::onCityDraftChanged,
                        onStateOrRegionChanged = viewModel::onStateOrRegionDraftChanged,
                        onPostCodeChanged = viewModel::onPostCodeDraftChanged,
                        onCountryCodeChanged = viewModel::onCountryCodeDraftChanged,
                        onBackToMap = viewModel::backToMapConfirm,
                        onConfirm = { viewModel.applyResolvedAddress(onSaved = onDone) }
                    )
                }
            }

            state.error?.takeIf { it.isNotBlank() }?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
