package net.metalbrain.paysmart.core.features.account.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import net.metalbrain.paysmart.core.features.account.creation.route.CreateAccountRoute
import net.metalbrain.paysmart.navigator.Screen

fun NavGraphBuilder.accountCreationRoutes(
    onCreateComplete: (dialCode: String, phoneNumber: String, countryIso2: String) -> Unit,
    onCreateSignIn: () -> Unit,
    onCreateHelp: () -> Unit,
    onCreateBack: () -> Unit,
    onOtpComplete: (countryIso2: String) -> Unit,
    onOtpBack: () -> Unit,
    onPostOtpCapabilitiesNext: (countryIso2: String) -> Unit,
    onPostOtpCapabilitiesBack: () -> Unit,
    onPostOtpSecurityStepsNext: (countryIso2: String) -> Unit,
    onPostOtpSecurityStepsBack: () -> Unit,
    onClientInformationContinue: () -> Unit,
    onClientInformationBack: () -> Unit,
    onMfaNudgePrimaryAction: (hasVerifiedEmail: Boolean) -> Unit,
    onMfaNudgeSkip: () -> Unit,
    onMfaNudgeBlocked: () -> Unit,
    onMfaNudgeBack: () -> Unit,
) {
    composable(Screen.CreateAccount.route) {
        CreateAccountRoute(
            onVerificationContinue = onCreateComplete,
            onGetHelp = onCreateHelp,
            onSignIn = onCreateSignIn,
            onBack = onCreateBack,
        )
    }
    // OtpVerification, PostOtpCapabilities, PostOtpSecuritySteps,
    // PostOtpClientInformation, PostOtpMfaNudge routes migrate here once
    // their screens move from :app to :feature:account.
}
