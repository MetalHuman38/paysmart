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

/**
 * Composable screen that displays the security steps to be completed after the OTP verification
 * during the account creation process.
 *
 * @param countryIso2 The ISO 3166-1 alpha-2 code of the selected country used to resolve the market name and flag.
 * @param onBack Callback invoked when the user requests to navigate back to the previous screen.
 * @param onContinue Callback invoked when the user proceeds to the next step of the registration flow.
 */
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
