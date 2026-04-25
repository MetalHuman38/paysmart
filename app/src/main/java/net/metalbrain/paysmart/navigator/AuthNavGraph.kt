package net.metalbrain.paysmart.navigator

import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.metalbrain.paysmart.core.features.account.authentication.federated.route.LinkFederatedAccountRoute
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.navigation.accountAuthRoutes
import net.metalbrain.paysmart.core.features.account.navigation.accountCreationRoutes
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricOptInScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricSessionUnlock
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.ChangePasscodeBiometricGateScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.ChangePasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.SetPasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.VerifyPasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel.PasscodeViewModel
import net.metalbrain.paysmart.core.features.account.authorization.password.screen.CreateLocalPasswordScreen
import net.metalbrain.paysmart.core.features.account.authorization.password.screen.EnterPasswordScreen
import net.metalbrain.paysmart.core.features.account.authorization.password.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.core.features.account.authorization.password.viewmodel.EnterPasswordViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.OtpVerificationScreen
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.ReauthOtpScreen
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.OTPViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.core.features.account.creation.screen.ClientInformationScreen
import net.metalbrain.paysmart.core.features.account.creation.screen.PostOtpCapabilitiesScreen
import net.metalbrain.paysmart.core.features.account.creation.screen.PostOtpSecurityStepsScreen
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.PostOtpCapabilitiesViewModel
import net.metalbrain.paysmart.core.features.account.passkey.screen.PasskeySetupScreen
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePasswordRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePhoneRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.RecoverAccountScreen
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePasswordViewModel
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import net.metalbrain.paysmart.core.features.account.screen.AccountProtectionContent
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaNudgeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureGateScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureRequirement
import net.metalbrain.paysmart.core.features.language.screen.LanguageSelectionScreen
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.core.session.SessionViewModel
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.loader.LoadingPhase
import net.metalbrain.paysmart.ui.screens.startup.StartupScreen
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.utils.formatPhoneNumberForDisplay

internal fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    composable(Screen.Splash.route) {
        SplashScreen(phase = LoadingPhase.Startup)
    }

    composable(Screen.Startup.route) {
        val viewModel: LanguageViewModel = hiltViewModel()
        val loginViewModel: LoginViewModel = hiltViewModel()
        val hostActivity = LocalActivity.current as? FragmentActivity

        LaunchedEffect(hostActivity) {
            val activity = hostActivity ?: return@LaunchedEffect
            loginViewModel.signInWithPasskey(
                activity = activity,
                autoAttempt = true,
                onSuccess = {
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onError = {
                    Log.d("NavGraph", "Startup auto passkey attempt skipped: ${it.localizedMessage}")
                }
            )
        }

        StartupScreen(
            navController = navController,
            onLoginClick = {
                navController.navigateInGraph(Screen.Login.route)
            },
            onCreateAccountClick = {
                navController.navigateInGraph(Screen.CreateAccount.route)
            },
            viewModel = viewModel,
        )
    }

    composable(Screen.ProtectAccount.route) {
        AccountProtectionContent(
            onSetPasscodeClick = {
                navController.navigateInGraph(Screen.SetUpPassCode.route)
            },
            onSetBiometricClick = {
                navController.navigateInGraph(Screen.BiometricOptIn.route)
            },
            onSetPasskeyClick = {
                navController.navigateInGraph(Screen.PasskeySetup.route)
            }
        )
    }

    composable(Screen.PasskeySetup.route) {
        val viewModel: PasskeySetupViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val userState by userViewModel.uiState.collectAsState()
        val launchInterest = resolveLaunchInterest(userState)
        val isFromProtectAccount =
            navController.previousBackStackEntry?.destination?.route == Screen.ProtectAccount.route
        val activity = LocalActivity.current as FragmentActivity
        PasskeySetupScreen(
            activity = activity,
            viewModel = viewModel,
            onRegistered = {
                val targetRoute = if (isFromProtectAccount) {
                    createPasswordRouteForLaunchInterest(launchInterest)
                } else {
                    Screen.Home.route
                }
                navController.navigateInGraph(targetRoute) {
                    popUpTo(Screen.PasskeySetup.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.BiometricOptIn.route) {
        val viewModel: BiometricOptInViewModel = hiltViewModel()
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val userState by userViewModel.uiState.collectAsState()
        val launchInterest = resolveLaunchInterest(userState)
        val isFromProtectAccount =
            navController.previousBackStackEntry?.destination?.route == Screen.ProtectAccount.route
        val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
        val hasReadyPassword = localSettings?.passwordEnabled == true &&
            localSettings.localPasswordSetAt != null
        val activity = LocalActivity.current as FragmentActivity
        BiometricOptInScreen(
            activity = activity,
            viewModel = viewModel,
            onSuccess = {
                val targetRoute = if (isFromProtectAccount && hasReadyPassword) {
                    homeDestinationForLaunchInterest(launchInterest)
                } else if (hasReadyPassword) {
                    Screen.Home.route
                } else {
                    createPasswordRouteForLaunchInterest(launchInterest)
                }
                navController.navigateInGraph(targetRoute) {
                    popUpTo(Screen.ProtectAccount.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
    }

    composable(Screen.RequireSessionUnlock.route) {
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val sessionViewModel: SessionViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings

        val onUnlocked = {
            sessionViewModel.unlockSession {
                navController.navigateInGraph(Screen.Home.route) {
                    popUpTo(Screen.RequireSessionUnlock.route) { inclusive = true }
                }
            }
        }
        val onLogout = {
            userViewModel.signOut()
            navController.navigateClearingBackStackInGraph(
                route = Screen.Startup.route,
                source = "nav_graph_logout",
            )
        }

        when {
            localSecurityState is LocalSecurityState.Loading -> {
                SplashScreen(phase = LoadingPhase.Authentication)
            }

            localSettings?.biometricsEnabled == true -> {
                BiometricSessionUnlock(
                    onUnlock = onUnlocked,
                    onLogout = onLogout
                )
            }

            localSettings?.passcodeEnabled == true -> {
                VerifyPasscodeScreen(onVerified = onUnlocked)
            }

            localSettings?.passwordEnabled == true -> {
                val enterPasswordViewModel: EnterPasswordViewModel = hiltViewModel()
                EnterPasswordScreen(
                    viewModel = enterPasswordViewModel,
                    onPasswordCorrect = onUnlocked
                )
            }

            else -> {
                AccountProtectionContent(
                    onSetPasscodeClick = {
                        navController.navigateInGraph(Screen.SetUpPassCode.route) {
                            launchSingleTop = true
                        }
                    },
                    onSetBiometricClick = {
                        navController.navigateInGraph(Screen.BiometricOptIn.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }

    composable(
        route = Screen.Language.route,
        arguments = listOf(
            navArgument(Screen.Language.ORIGINARG) {
                type = NavType.StringType
                defaultValue = Screen.Origin.STARTUP.routeValue
            }
        )
    ) { backStackEntry ->
        val origin = Screen.Origin.fromRouteValue(
            backStackEntry.arguments?.getString(Screen.Language.ORIGINARG)
        )
        val viewModel: LanguageViewModel = hiltViewModel()
        val langCode by viewModel.currentLanguage.collectAsState()

        val selectedLang =
            supportedLanguages.find { it.code == langCode } ?: supportedLanguages.first()

        LanguageSelectionScreen(
            selectedLanguage = selectedLang,
            onLanguageSelected = { lang ->
                viewModel.setLanguage(lang.code)
            },
            onContinue = {
                val targetRoute = resolveLanguageContinueRoute(origin)
                navController.navigateInGraph(targetRoute) {
                    popUpTo(backStackEntry.destination.id) { inclusive = true }
                    launchSingleTop = origin == Screen.Origin.PROFILE_ACCOUNT_INFORMATION
                }
            },
            onBack = {
                navController.popBackStack()
            },
            modifier = Modifier
        )
    }

    composable(
        route = Screen.OtpVerification.route,
        arguments = listOf(
            navArgument("dialCode") { type = NavType.StringType },
            navArgument("phoneNumber") { type = NavType.StringType },
            navArgument(Screen.OtpVerification.COUNTRY_ISO2_ARG) {
                type = NavType.StringType
                defaultValue = DEFAULT_COUNTRY_ISO2
            }
        )
    ) {
        val otpViewModel: OTPViewModel = hiltViewModel()
        val dialCode = it.arguments?.getString("dialCode") ?: ""
        val rawPhone = it.arguments?.getString("phoneNumber") ?: ""
        val countryIso2 = normalizeCountryIso2(
            it.arguments?.getString(Screen.OtpVerification.COUNTRY_ISO2_ARG)
        )
        val formattedNumber = formatPhoneNumberForDisplay(
            rawNumber = rawPhone,
            dialCode = dialCode
        )
        otpViewModel.setFormattedPhoneNumber(formattedNumber)
        OtpVerificationScreen(
            phoneNumber = formattedNumber,
            onContinue = {
                navController.navigateInGraph(Screen.PostOtpCapabilities.routeWithCountry(countryIso2)) {
                    popUpTo(Screen.OtpVerification.route) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            viewModel = otpViewModel,
        )
    }

    composable(
        route = Screen.PostOtpCapabilities.route,
        arguments = listOf(
            navArgument("countryIso2") {
                type = NavType.StringType
                defaultValue = DEFAULT_COUNTRY_ISO2
            }
        )
    ) { backStackEntry ->
        val capabilitiesViewModel: PostOtpCapabilitiesViewModel = hiltViewModel()
        val countryIso2 = normalizeCountryIso2(
            backStackEntry.arguments?.getString("countryIso2")
        )
        val currentDestinationId = backStackEntry.destination.id

        PostOtpCapabilitiesScreen(
            countryIso2 = countryIso2,
            viewModel = capabilitiesViewModel,
            onBack = { navController.popBackStack() },
            onNext = {
                navController.navigateInGraph(Screen.PostOtpSecuritySteps.routeWithCountry(countryIso2)) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = Screen.PostOtpSecuritySteps.route,
        arguments = listOf(
            navArgument("countryIso2") {
                type = NavType.StringType
                defaultValue = DEFAULT_COUNTRY_ISO2
            }
        )
    ) { backStackEntry ->
        val countryIso2 = normalizeCountryIso2(
            backStackEntry.arguments?.getString("countryIso2")
        )
        val currentDestinationId = backStackEntry.destination.id

        PostOtpSecurityStepsScreen(
            countryIso2 = countryIso2,
            onBack = { navController.popBackStack() },
            onContinue = {
                navController.navigateInGraph(Screen.PostOtpClientInformation.routeWithCountry(countryIso2)) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = Screen.PostOtpClientInformation.route,
        arguments = listOf(
            navArgument("countryIso2") {
                type = NavType.StringType
                defaultValue = DEFAULT_COUNTRY_ISO2
            }
        )
    ) { backStackEntry ->
        val countryIso2 = normalizeCountryIso2(
            backStackEntry.arguments?.getString("countryIso2")
        )
        val clientInfoViewModel: ClientInformationViewModel = hiltViewModel()
        val currentDestinationId = backStackEntry.destination.id

        ClientInformationScreen(
            countryIso2 = countryIso2,
            viewModel = clientInfoViewModel,
            onBack = { navController.popBackStack() },
            onContinue = {
                navController.navigateInGraph(
                    Screen.LinkFederatedAccount.routeWithReturn("")
                ) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
            }
        )
    }

    composable(
        route = Screen.PostOtpMfaNudge.route,
        arguments = listOf(
            navArgument("countryIso2") {
                type = NavType.StringType
                defaultValue = DEFAULT_COUNTRY_ISO2
            }
        )
    ) { backStackEntry ->
        val countryIso2 = normalizeCountryIso2(
            backStackEntry.arguments?.getString("countryIso2")
        )
        val mfaViewModel: MfaNudgeViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val currentDestinationId = backStackEntry.destination.id

        MfaNudgeScreen(
            viewModel = mfaViewModel,
            onBack = { navController.popBackStack() },
            onPrimaryAction = { hasVerifiedEmail ->
                val next = if (hasVerifiedEmail) {
                    Screen.CreatePassword.BASEROUTE
                } else {
                    Screen.LinkFederatedAccount.routeWithReturn("")
                }
                navController.navigateInGraph(next) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
            },
            onBlockedAction = {
                userViewModel.signOut()
                navController.navigateClearingBackStackInGraph(
                    route = Screen.Login.route,
                    source = "mfa_unsupported_first_factor",
                )
            },
            onSkip = {
                navController.navigateInGraph(
                    Screen.LinkFederatedAccount.routeWithReturn("")
                ) {
                    popUpTo(currentDestinationId) { inclusive = true }
                }
            }
        )
    }

    accountCreationRoutes(
        onCreateComplete = { dialCode, phoneNumber, countryIso2 ->
            navController.navigateInGraph(
                Screen.OtpVerification.routeWithArgs(
                    dialCode = dialCode,
                    phoneNumber = phoneNumber,
                    countryIso2 = countryIso2
                )
            )
        },
        onCreateSignIn = {
            navController.navigateInGraph(Screen.Login.route) { launchSingleTop = true }
        },
        onCreateHelp = {
            navController.navigateInGraph(Screen.Help.route) { launchSingleTop = true }
        },
        onCreateBack = { navController.popBackStack() },
        onOtpComplete = {},
        onOtpBack = {},
        onPostOtpCapabilitiesNext = {},
        onPostOtpCapabilitiesBack = {},
        onPostOtpSecurityStepsNext = {},
        onPostOtpSecurityStepsBack = {},
        onClientInformationContinue = {},
        onClientInformationBack = {},
        onMfaNudgePrimaryAction = {},
        onMfaNudgeSkip = {},
        onMfaNudgeBlocked = {},
        onMfaNudgeBack = {},
    )

    composable(
        route = "${Screen.LinkFederatedAccount.route}?${Screen.LinkFederatedAccount.RETURN_ROUTE_ARG}={${Screen.LinkFederatedAccount.RETURN_ROUTE_ARG}}",
        arguments = listOf(
            navArgument(Screen.LinkFederatedAccount.RETURN_ROUTE_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
        val hasReadyPassword = localSettings?.passwordEnabled == true &&
            localSettings.localPasswordSetAt != null
        val hasRecoveryMethod = localSettings?.recoveryMethodReady == true ||
            localSettings?.hasVerifiedEmail == true

        LinkFederatedAccountRoute(
            clientId = stringResource(R.string.default_web_client_id),
            returnRoute = Uri.decode(
                backStackEntry.arguments
                    ?.getString(Screen.LinkFederatedAccount.RETURN_ROUTE_ARG)
                    .orEmpty()
            ),
            hasReadyPassword = hasReadyPassword,
            hasRecoveryMethod = hasRecoveryMethod,
            onRecoveryMethodReady = { securityViewModel.markRecoveryMethodReady() },
            onNavigateToDestination = { destination ->
                navController.navigateInGraph(destination) {
                    popUpTo(backStackEntry.destination.id) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToPasskeySetup = {
                navController.navigateInGraph(Screen.PasskeySetup.route) {
                    launchSingleTop = true
                }
            },
            onNavigateToAddEmail = {
                navController.navigateInGraph(Screen.AddEmail.route) {
                    launchSingleTop = true
                }
            },
        )
    }

    accountAuthRoutes(
        clientId = { stringResource(R.string.default_web_client_id) },
        currentLanguage = {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val currentLang by languageViewModel.currentLanguage.collectAsState()
            currentLang
        },
        onLoginSuccess = {
            navController.navigateInGraph(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        },
        onLoginMfaRequired = {
            navController.navigateInGraph(Screen.LoginMfaChallenge.route) {
                launchSingleTop = true
            }
        },
        onLoginForgotPassword = {
            navController.navigateInGraph(Screen.RecoverAccount.routeWithOrigin("login"))
        },
        onLoginSignUp = {
            navController.navigateInGraph(Screen.CreateAccount.route)
        },
        onLoginBack = { navController.popBackStack() },
        onLoginNavigateToLanguage = {
            navController.navigateInGraph(Screen.Language.routeWithOrigin(Screen.Origin.LOGIN))
        },
        onLoginNavigateToReauth = {
            navController.navigate(Screen.Reauthenticate.BASEROUTES)
        },
        onLoginEmailSent = { email ->
            navController.navigate(Screen.EmailSent.routeWithEmail(email))
        },
        onMfaChallengeBack = { navController.popBackStack() },
        onMfaChallengeSuccess = {
            navController.navigateInGraph(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        },
        onAddEmailSent = { email, returnRoute ->
            navController.navigateInGraph(
                Screen.EmailSent.routeWithArgs(email = email, returnRoute = returnRoute)
            ) { launchSingleTop = true }
        },
        onMarkRecoveryReady = {
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val markRecoveryReady: () -> Unit = {
                securityViewModel.markRecoveryMethodReady()
            }
            markRecoveryReady
        },
        onEmailSentNavigateToVerified = { returnRoute ->
            navController.navigateInGraph(Screen.EmailVerified.routeWithReturn(returnRoute)) {
                launchSingleTop = true
            }
        },
        onEmailSentBack = { navController.popBackStack() },
        onEmailVerifiedBackToApp = { returnRoute ->
            navController.navigateClearingBackStackInGraph(
                route = returnRoute,
                source = "email_verification_success",
            )
        },
    )

    composable(
        route = Screen.Reauthenticate.route,
        arguments = listOf(
            navArgument("target") {
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val viewModel: ReauthOtpViewModel = hiltViewModel()
        val activity = LocalActivity.current as FragmentActivity
        val recoveryTarget = backStackEntry.arguments
            ?.getString("target")
            ?.takeIf { it.isNotBlank() }
            ?.let(Uri::decode)

        ReauthOtpScreen(
            viewModel = viewModel,
            activity = activity,
            onSuccess = {
                if (recoveryTarget != null) {
                    navController.navigateInGraph(recoveryTarget) {
                        popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                    }
                } else {
                    navController.navigateInGraph(Screen.EnterPassword.route) {
                        popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                    }
                }
            },
            onBack = {
                navController.popBackStack()
            }
        )
    }

    composable(Screen.EnterPassword.route) {
        val viewModel: EnterPasswordViewModel = hiltViewModel()
        EnterPasswordScreen(
            viewModel = viewModel,
            onPasswordCorrect = {
                navController.navigateInGraph(Screen.Home.route) {
                    popUpTo(Screen.EnterPassword.route) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = Screen.RecoverAccount.route,
        arguments = listOf(
            navArgument("origin") {
                defaultValue = "in_app"
            }
        )
    ) {
        RecoverAccountScreen(
            onBackClick = { navController.popBackStack() },
            onHelpClick = { /* Show Help Dialog or Navigate */ },
            onChangePasswordClick = {
                navController.navigateInGraph(
                    Screen.Reauthenticate.routeWithTarget(
                        Screen.ChangePasswordRecovery.route
                    )
                )
            },
            onChangePhoneClick = {
                navController.navigateInGraph(
                    Screen.Reauthenticate.routeWithTarget(
                        Screen.ChangePhoneRecovery.route
                    )
                )
            }
        )
    }

    composable(Screen.ChangePasswordRecovery.route) {
        val viewModel: ChangePasswordViewModel = hiltViewModel()
        ChangePasswordRecoveryScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onSuccess = { navController.popBackStack() }
        )
    }

    composable(Screen.ChangePhoneRecovery.route) {
        val viewModel: ChangePhoneRecoveryViewModel = hiltViewModel()
        val activity = LocalActivity.current as FragmentActivity
        ChangePhoneRecoveryScreen(
            viewModel = viewModel,
            activity = activity,
            onBack = { navController.popBackStack() },
            onSuccess = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.CreatePassword.route,
        arguments = listOf(
            navArgument(Screen.CreatePassword.RETURN_ROUTE_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        val viewModel: CreatePasswordViewModel = hiltViewModel()
        val returnRoute = Uri.decode(
            backStackEntry.arguments
                ?.getString(Screen.CreatePassword.RETURN_ROUTE_ARG)
                .orEmpty()
        )
        val destination = returnRoute.ifBlank { Screen.Home.route }
        CreateLocalPasswordScreen(
            viewModel = viewModel,
            onDone = {
                navController.navigateClearingBackStackInGraph(
                    route = destination,
                    source = "create_password_complete",
                )
            }
        )
    }

    composable(Screen.SetUpPassCode.route) {
        val passCodeviewModel: PasscodeViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val userState by userViewModel.uiState.collectAsState()
        val launchInterest = resolveLaunchInterest(userState)
        val isFromProtectAccount =
            navController.previousBackStackEntry?.destination?.route == Screen.ProtectAccount.route
        SetPasscodeScreen(
            viewModel = passCodeviewModel,
            onPasscodeSet = {
                Log.d("SetPasscodeScreen", "onPasscodeSet invoked")
                val targetRoute = if (isFromProtectAccount) {
                    createPasswordRouteForLaunchInterest(launchInterest)
                } else {
                    Screen.Home.route
                }
                navController.navigateInGraph(targetRoute) {
                    popUpTo(Screen.SetUpPassCode.route) { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.ProfileChangePasscodeGate.route) {
        ChangePasscodeBiometricGateScreen(
            onBack = { navController.popBackStack() },
            onVerified = {
                navController.navigateInGraph(Screen.ProfileChangePasscode.route) {
                    popUpTo(Screen.ProfileChangePasscodeGate.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    composable(Screen.ProfileChangePasscode.route) {
        ChangePasscodeScreen(
            onBack = { navController.popBackStack() },
            onPasscodeChanged = {
                navController.popBackStack()
            }
        )
    }

    composable(Screen.VerifyPasscode.route) {
        VerifyPasscodeScreen(
            onVerified = {
                navController.navigateInGraph(Screen.Home.route) {
                    popUpTo(Screen.VerifyPasscode.route) { inclusive = true }
                }
            },
        )
    }

    composable(
        route = Screen.FeatureGate.route,
        arguments = listOf(
            navArgument(Screen.FeatureGate.FEATUREARG) {
                type = NavType.StringType
                defaultValue = FeatureKey.ADD_MONEY.id
            },
            navArgument(Screen.FeatureGate.RESUMEROUTEARG) {
                type = NavType.StringType
                defaultValue = Screen.Home.route
            }
        )
    ) { backStackEntry ->
        val feature = FeatureKey.fromId(
            backStackEntry.arguments?.getString(Screen.FeatureGate.FEATUREARG)
        )
        val resumeRoute = Uri.decode(
            backStackEntry.arguments?.getString(Screen.FeatureGate.RESUMEROUTEARG)
                ?: Screen.Home.route
        )
        val gateRoute = Screen.FeatureGate.routeWithArgs(feature.id, resumeRoute)

        val securityViewModel: SecurityViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings
        val decision = settings?.let {
            FeatureAccessPolicy.evaluate(feature, it)
        }

        LaunchedEffect(localSecurityState, decision?.isAllowed, feature, resumeRoute) {
            Log.d(
                "FeatureGateDiag",
                "feature=${feature.id} " +
                    "state=${localSecurityState::class.simpleName} " +
                    "resumeRoute=$resumeRoute " +
                    "settingsReady=${settings != null} " +
                    "allowed=${decision?.isAllowed} " +
                    "missing=${decision?.missingRequirements.orEmpty()} " +
                    "strength=${decision?.currentSecurityStrength} " +
                    "required=${decision?.requiredSecurityStrength}"
            )
        }

        LaunchedEffect(decision?.isAllowed, resumeRoute) {
            if (decision?.isAllowed == true) {
                navController.navigateInGraph(resumeRoute) {
                    popUpTo(backStackEntry.destination.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        if (localSecurityState is LocalSecurityState.Loading || decision == null) {
            SplashScreen(phase = LoadingPhase.FetchingData)
            return@composable
        }

        FeatureGateScreen(
            feature = feature,
            decision = decision,
            onContinue = {
                when (decision.nextRequirement) {
                    FeatureRequirement.VERIFIED_EMAIL -> {
                        navController.navigateInGraph(
                            "${Screen.AddEmail.route}?returnRoute=${Uri.encode(gateRoute)}"
                        )
                    }

                    FeatureRequirement.HOME_ADDRESS_VERIFIED -> {
                        navController.navigateInGraph(Screen.ProfileAddressResolver.route)
                    }

                    FeatureRequirement.IDENTITY_VERIFIED -> {
                        navController.navigateInGraph(Screen.ProfileIdentityResolver.route)
                    }

                    FeatureRequirement.SECURITY_STRENGTH_TWO -> {
                        navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route) {
                            launchSingleTop = true
                        }
                    }

                    null -> {
                        navController.navigateInGraph(resumeRoute) {
                            popUpTo(backStackEntry.destination.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            },
            onBack = { navController.popBackStack() }
        )
    }
}
