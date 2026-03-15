package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.account.creation.components.AccountCreationScaffold
import net.metalbrain.paysmart.core.features.account.creation.components.PostOtpSecurityContent
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun PostOtpSecurityStepsScreen(
    countryIso2: String,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val market = CountrySelectionCatalog.countryByIso2(context, countryIso2)

    AccountCreationScaffold(onBack = onBack) { innerPadding ->
        PostOtpSecurityContent(
            countryName = market?.name ?: countryIso2,
            flagEmoji = market?.flagEmoji
                ?: CountrySelectionCatalog.flagForCountry(context, countryIso2),
            onContinue = onContinue,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.space6)
        )
    }
}
