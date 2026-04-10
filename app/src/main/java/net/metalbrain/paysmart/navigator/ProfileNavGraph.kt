package net.metalbrain.paysmart.navigator

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.address.screen.AddressSetupResolverScreen
import net.metalbrain.paysmart.core.features.account.address.viewmodel.AddressSetupResolverViewModel
import net.metalbrain.paysmart.core.features.account.passkey.screen.ProfilePasskeySettingsScreen
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountInformationScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitDetailsRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitsRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountStatementRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileAboutScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileAboutSocialsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileConnectedAccountsRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileDetailsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfilePrivacySettingsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfilePhotoPickerRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSecurityPrivacyScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSubPageScreen
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.AccountStatementViewModel
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfilePhotoViewModel
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfileStateViewModel
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaNudgeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.core.features.account.authorization.biometric.provider.BiometricHelper
import net.metalbrain.paysmart.core.features.cards.viewmodel.ManagedCardsViewModel
import net.metalbrain.paysmart.core.features.fundingaccount.viewmodel.FundingAccountViewModel
import net.metalbrain.paysmart.core.features.help.utils.openExternalUri
import net.metalbrain.paysmart.core.features.identity.screen.IdentityInfoScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityPendingReviewScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityThirdPartyProviderScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityUploadScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityVerifyScreen
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityProviderHandoffViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.core.features.theme.viewmodel.AppThemeViewModel
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.room.manager.RoomKeyManager
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

internal fun NavGraphBuilder.profileNavGraph(
    navController: NavHostController
) {
    composable(Screen.ProfileScreen.route) {
        val userViewModel: UserViewModel = hiltViewModel()
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val state by userViewModel.uiState.collectAsState()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val verifiedFromServer =
            (localSecurityState as? LocalSecurityState.Ready)?.settings?.hasVerifiedEmail == true

        if (state is UserUiState.ProfileLoaded) {
            ProfileScreen(
                user = (state as UserUiState.ProfileLoaded).user,
                isVerified = verifiedFromServer,
                showVersionLabel = BuildConfig.DEBUG,
                onChangePhotoClick = {
                    navController.navigateInGraph(Screen.ProfilePhotoPicker.route)
                },
                onAccountInformationClick = {
                    navController.navigateInGraph(Screen.ProfileAccountInformation.route)
                },
                onSecurityPrivacyClick = {
                    navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route)
                },
                onConnectedAccountsClick = {
                    navController.navigateInGraph(Screen.ProfileConnectedAccounts.route)
                },
                onHelpAndSupportClick = {
                    navController.navigateInGraph(Screen.Help.route)
                },
                onAboutClick = {
                    navController.navigateInGraph(Screen.ProfileAbout.route)
                },
                onLogout = {
                    userViewModel.signOut()
                    RoomKeyManager.deleteKey()
                    navController.navigateClearingBackStackInGraph(
                        route = Screen.Startup.route,
                        source = "profile_logout",
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.ProfilePhotoPicker.route) {
        val userViewModel: UserViewModel = hiltViewModel()
        val photoViewModel: ProfilePhotoViewModel = hiltViewModel()
        val userState by userViewModel.uiState.collectAsState()
        val photoState by photoViewModel.uiState.collectAsState()

        if (userState is UserUiState.ProfileLoaded) {
            ProfilePhotoPickerRoute(
                user = (userState as UserUiState.ProfileLoaded).user,
                uiState = photoState,
                viewModel = photoViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.ProfileAccountInformation.route) {
        val languageViewModel: LanguageViewModel = hiltViewModel()
        val profileViewModel: ProfileStateViewModel = hiltViewModel()
        val appThemeViewModel: AppThemeViewModel = hiltViewModel()
        val profileState by profileViewModel.uiState.collectAsState()
        val languageCode by languageViewModel.currentLanguage.collectAsState()
        val themeMode by appThemeViewModel.themeMode.collectAsState()
        val themeVariant by appThemeViewModel.themeVariant.collectAsState()

        AccountInformationScreen(
            currentLanguage = languageCode,
            currentThemeMode = themeMode,
            currentThemeVariant = themeVariant,
            profileStatusLabel = when {
                profileState.isLocked -> stringResource(R.string.profile_status_locked)
                profileState.isIncomplete -> stringResource(R.string.profile_status_incomplete)
                else -> stringResource(R.string.profile_status_complete)
            },
            onBack = { navController.popBackStack() },
            onProfileClick = {
                navController.navigateInGraph(Screen.ProfileIdentity.route)
            },
            onAccountLimitsClick = {
                navController.navigateInGraph(Screen.ProfileAccountLimits.route)
            },
            onAccountStatementClick = {
                navController.navigateInGraph(Screen.ProfileAccountStatement.route)
            },
            onLanguageClick = {
                navController.navigateInGraph(
                    Screen.Language.routeWithOrigin(
                        Screen.Origin.PROFILE_ACCOUNT_INFORMATION
                    )
                )
            },
            onThemeModeClick = {
                appThemeViewModel.cycleThemeMode()
            },
            onThemeVariantClick = {
                appThemeViewModel.cycleThemeVariant()
            }
        )
    }

    composable(Screen.ProfileSecurityPrivacy.route) {
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val hideBalanceEnabled by securityViewModel.hideBalanceEnabled.collectAsState()
        val context = LocalContext.current
        val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings

        ProfileSecurityPrivacyScreen(
            settings = settings,
            hideBalanceEnabled = hideBalanceEnabled,
            onBack = { navController.popBackStack() },
            onResetPassword = {
                navController.navigateInGraph(
                    Screen.Reauthenticate.routeWithTarget(
                        Screen.ChangePasswordRecovery.route
                    )
                ) {
                    launchSingleTop = true
                }
            },
            onTransactionPin = {
                val isPasscodeReady =
                    settings?.passcodeEnabled == true && settings.localPassCodeSetAt != null
                val shouldUseBiometricGate =
                    settings?.biometricsEnabled == true &&
                        BiometricHelper.isBiometricAvailable(context)
                val targetRoute = when {
                    !isPasscodeReady -> Screen.SetUpPassCode.route
                    shouldUseBiometricGate -> Screen.ProfileChangePasscodeGate.route
                    else -> Screen.ProfileChangePasscode.route
                }
                navController.navigateInGraph(targetRoute) {
                    launchSingleTop = true
                }
            },
            onMfaSettings = {
                navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                    launchSingleTop = true
                }
            },
            onPasskeySettings = {
                navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
                    launchSingleTop = true
                }
            },
            onBiometricToggle = { enabled ->
                if (enabled) {
                    navController.navigateInGraph(Screen.BiometricOptIn.route) {
                        launchSingleTop = true
                    }
                } else {
                    securityViewModel.clearBiometricOptIn()
                }
            },
            onViewPrivacySettings = {
                navController.navigateInGraph(Screen.ProfilePrivacySettings.route) {
                    launchSingleTop = true
                }
            },
            onHideBalanceToggle = securityViewModel::setHideBalance,
        )
    }

    composable(Screen.ProfilePrivacySettings.route) {
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val privacyCreditEnabled by securityViewModel.privacyCreditEnabled.collectAsState()
        val privacySocialMediaEnabled by securityViewModel.privacySocialMediaEnabled.collectAsState()
        val context = LocalContext.current
        val termsOfUseUrl = stringResource(R.string.terms_of_use)
        val openActionFailedMessage = stringResource(R.string.profile_about_open_action_failed)

        ProfilePrivacySettingsScreen(
            privacyCreditEnabled = privacyCreditEnabled,
            privacySocialMediaEnabled = privacySocialMediaEnabled,
            onBack = { navController.popBackStack() },
            onPrivacyCreditToggle = securityViewModel::setPrivacyCreditEnabled,
            onPrivacySocialMediaToggle = securityViewModel::setPrivacySocialMediaEnabled,
            onOpenTermsOfUse = {
                if (!openExternalUri(context, termsOfUseUrl)) {
                    Toast.makeText(context, openActionFailedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    composable(Screen.ProfilePasskeySettings.route) {
        val viewModel: PasskeySetupViewModel = hiltViewModel()
        val activity = LocalActivity.current as FragmentActivity
        ProfilePasskeySettingsScreen(
            activity = activity,
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.ProfileMfaNudge.route) { backStackEntry ->
        val mfaViewModel: MfaNudgeViewModel = hiltViewModel()
        val userViewModel: UserViewModel = hiltViewModel()
        val currentDestinationId = backStackEntry.destination.id

        MfaNudgeScreen(
            viewModel = mfaViewModel,
            onBack = { navController.popBackStack() },
            onPrimaryAction = { hasVerifiedEmail ->
                if (hasVerifiedEmail) {
                    navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route) {
                        popUpTo(currentDestinationId) { inclusive = true }
                    }
                } else {
                    navController.navigateInGraph(
                        "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileMfaNudge.route)}"
                    )
                }
            },
            onBlockedAction = {
                userViewModel.signOut()
                navController.navigateClearingBackStackInGraph(
                    route = Screen.Login.route,
                    source = "mfa_unsupported_first_factor",
                )
            },
            onSkip = { navController.popBackStack() }
        )
    }

    composable(Screen.ProfileConnectedAccounts.route) {
        val viewModel: FundingAccountViewModel = hiltViewModel()
        val managedCardsViewModel: ManagedCardsViewModel = hiltViewModel()
        ProfileConnectedAccountsRoute(
            viewModel = viewModel,
            managedCardsViewModel = managedCardsViewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.ProfileAbout.route) {
        val context = LocalContext.current
        val actionOpenFailedMessage = stringResource(R.string.profile_about_open_action_failed)
        val legalUrl = stringResource(R.string.profile_about_legal_url)
        val blogUrl = stringResource(R.string.profile_about_blog_url)
        val contactUrl = stringResource(R.string.profile_about_contact_url)
        val contactEmail = stringResource(R.string.profile_about_contact_email)

        fun openUriOrToast(uri: String) {
            if (!openExternalUri(context, uri)) {
                Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        ProfileAboutScreen(
            onBack = { navController.popBackStack() },
            onLegalClick = { openUriOrToast(legalUrl) },
            onSocialMediaClick = {
                navController.navigateInGraph(Screen.ProfileAboutSocials.route) {
                    launchSingleTop = true
                }
            },
            onBlogClick = { openUriOrToast(blogUrl) },
            onAppRatingClick = {
                if (!openAppRating(context)) {
                    Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                }
            },
            onContactUsClick = {
                val openedEmail = openSupportEmailComposer(context, contactEmail)
                if (!openedEmail && !openExternalUri(context, contactUrl)) {
                    Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    composable(Screen.ProfileAboutSocials.route) {
        val context = LocalContext.current
        val actionOpenFailedMessage = stringResource(R.string.profile_about_open_action_failed)
        val xUrl = stringResource(R.string.profile_about_social_x_url)
        val instagramUrl = stringResource(R.string.profile_about_social_instagram_url)
        val linkedInUrl = stringResource(R.string.profile_about_social_linkedin_url)
        val facebookUrl = stringResource(R.string.profile_about_social_facebook_url)

        fun openUriOrToast(uri: String) {
            if (!openExternalUri(context, uri)) {
                Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
            }
        }

        ProfileAboutSocialsScreen(
            onBack = { navController.popBackStack() },
            onXClick = { openUriOrToast(xUrl) },
            onInstagramClick = { openUriOrToast(instagramUrl) },
            onLinkedInClick = { openUriOrToast(linkedInUrl) },
            onFacebookClick = { openUriOrToast(facebookUrl) }
        )
    }

    composable(Screen.ProfileIdentity.route) {
        val profileViewModel: ProfileStateViewModel = hiltViewModel()
        val profileState by profileViewModel.uiState.collectAsState()
        val profile = profileState.user

        if (profile != null) {
            ProfileDetailsScreen(
                user = profile,
                isLocked = profileState.isLocked,
                missingItems = profileState.missingItems,
                nextStep = profileState.nextStep,
                showVersionLabel = true,
                onResolveSetup = {
                    when (profileState.nextStep) {
                        ProfileNextStep.VERIFY_EMAIL -> {
                            navController.navigateInGraph(
                                "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileIdentity.route)}"
                            )
                        }

                        ProfileNextStep.COMPLETE_ADDRESS,
                        ProfileNextStep.VERIFY_IDENTITY -> {
                            navController.navigateInGraph(
                                if (profileState.nextStep == ProfileNextStep.COMPLETE_ADDRESS) {
                                    Screen.ProfileAddressResolver.route
                                } else {
                                    Screen.ProfileIdentityResolver.route
                                }
                            )
                        }

                        ProfileNextStep.REVIEW_PROFILE,
                        null -> Unit
                    }
                },
                showMfaNudgeCta = profileState.security?.hasSkippedMfaEnrollmentPrompt == true,
                onOpenMfaNudge = {
                    navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                        launchSingleTop = true
                    }
                },
                showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                onOpenPasskeyNudge = {
                    navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        } else {
            ProfileSubPageScreen(
                title = stringResource(R.string.profile_details_title),
                description = stringResource(R.string.profile_details_loading_message),
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.OnboardingProfile.route) {
        val profileViewModel: ProfileStateViewModel = hiltViewModel()
        val profileState by profileViewModel.uiState.collectAsState()
        val profile = profileState.user

        if (profile != null) {
            ProfileDetailsScreen(
                user = profile,
                isLocked = profileState.isLocked,
                missingItems = profileState.missingItems,
                nextStep = profileState.nextStep,
                showVersionLabel = true,
                onResolveSetup = {
                    when (profileState.nextStep) {
                        ProfileNextStep.VERIFY_EMAIL -> {
                            navController.navigateInGraph(
                                Screen.LinkFederatedAccount.routeWithReturn(Screen.OnboardingProfile.route)
                            )
                        }

                        ProfileNextStep.COMPLETE_ADDRESS,
                        ProfileNextStep.VERIFY_IDENTITY -> {
                            navController.navigateInGraph(
                                if (profileState.nextStep == ProfileNextStep.COMPLETE_ADDRESS) {
                                    Screen.ProfileAddressResolver.route
                                } else {
                                    Screen.ProfileIdentityResolver.route
                                }
                            )
                        }

                        ProfileNextStep.REVIEW_PROFILE,
                        null -> Unit
                    }
                },
                showSecuritySetupCta = true,
                onContinueToSecuritySetup = {
                    navController.navigateInGraph(Screen.ProtectAccount.route) {
                        popUpTo(Screen.OnboardingProfile.route) { inclusive = true }
                    }
                },
                showMfaNudgeCta = profileState.security?.hasSkippedMfaEnrollmentPrompt == true,
                onOpenMfaNudge = {
                    navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                        launchSingleTop = true
                    }
                },
                showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                onOpenPasskeyNudge = {
                    navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        } else {
            ProfileSubPageScreen(
                title = stringResource(R.string.profile_details_title),
                description = stringResource(R.string.profile_details_loading_message),
                onBack = { navController.popBackStack() }
            )
        }
    }

    composable(Screen.ProfileAddressResolver.route) {
        val resolverViewModel: AddressSetupResolverViewModel = hiltViewModel()
        AddressSetupResolverScreen(
            viewModel = resolverViewModel,
            onBack = { navController.popBackStack() },
            onDone = { navController.popBackStack() }
        )
    }

    navigation(
        route = Screen.ProfileIdentityResolver.route,
        startDestination = Screen.ProfileIdentityResolverIntro.route
    ) {
        composable(
            route = Screen.ProfileIdentityResolverThirdParty.route,
            arguments = listOf(
                navArgument(Screen.ProfileIdentityResolverThirdParty.EVENT_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Screen.ProfileIdentityResolverThirdParty.SESSION_ID_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Screen.ProfileIdentityResolverThirdParty.PROVIDER_REF_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
            }
            val providerViewModel: IdentityProviderHandoffViewModel = hiltViewModel(parentEntry)
            val activity = LocalActivity.current as? FragmentActivity
            val event = backStackEntry.arguments
                ?.getString(Screen.ProfileIdentityResolverThirdParty.EVENT_ARG)
                .orEmpty()
            val sessionId = backStackEntry.arguments
                ?.getString(Screen.ProfileIdentityResolverThirdParty.SESSION_ID_ARG)
                ?.takeIf { it.isNotBlank() }
            val providerRef = backStackEntry.arguments
                ?.getString(Screen.ProfileIdentityResolverThirdParty.PROVIDER_REF_ARG)
                ?.takeIf { it.isNotBlank() }
            val deepLink = activity?.intent?.data?.toString()

            IdentityThirdPartyProviderScreen(
                viewModel = providerViewModel,
                callbackEvent = event,
                callbackSessionId = sessionId,
                callbackProviderRef = providerRef,
                callbackDeepLink = deepLink,
                onBack = { navController.popBackStack() },
                onFallbackToLocal = {
                    navController.navigateInGraph(Screen.ProfileIdentityResolverVerify.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileIdentityResolverIntro.route) {
            IdentityInfoScreen(
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigateInGraph(Screen.ProfileIdentityResolverVerify.route) {
                        launchSingleTop = true
                    }
                },
                onHelp = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileIdentityResolverVerify.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
            }
            val resolverViewModel: IdentitySetupResolverViewModel = hiltViewModel(parentEntry)
            IdentityVerifyScreen(
                viewModel = resolverViewModel,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigateInGraph(Screen.ProfileIdentityResolverUpload.route) {
                        launchSingleTop = true
                    }
                },
                onHelp = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileIdentityResolverUpload.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
            }
            val resolverViewModel: IdentitySetupResolverViewModel = hiltViewModel(parentEntry)
            IdentityUploadScreen(
                viewModel = resolverViewModel,
                onBackToVerify = { navController.popBackStack() },
                onPendingReview = {
                    navController.navigateInGraph(Screen.ProfileIdentityResolverPending.route) {
                        popUpTo(Screen.ProfileIdentityResolverUpload.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileIdentityResolverPending.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
            }
            val resolverViewModel: IdentitySetupResolverViewModel = hiltViewModel(parentEntry)
            IdentityPendingReviewScreen(
                viewModel = resolverViewModel,
                onDone = {
                    navController.popBackStack(
                        Screen.ProfileIdentityResolver.route,
                        inclusive = true
                    )
                },
                onHelp = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }

    composable(Screen.ProfileAccountLimits.route) {
        val securityViewModel: SecurityViewModel = hiltViewModel()
        val localSecurityState by securityViewModel.localSecurityState.collectAsState()
        val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings

        AccountLimitsRoute(
            settings = settings,
            onBack = { navController.popBackStack() },
            onAccountClick = { currencyCode ->
                navController.navigateInGraph(
                    Screen.ProfileAccountLimitsDetails.routeWithCurrency(currencyCode)
                ) {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(
        route = Screen.ProfileAccountLimitsDetails.route,
        arguments = listOf(
            navArgument(Screen.ProfileAccountLimitsDetails.CURRENCYARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        AccountLimitDetailsRoute(
            onBack = { navController.popBackStack() },
            onHelp = {
                navController.navigateInGraph(Screen.Help.route) {
                    launchSingleTop = true
                }
            }
        )
    }

    composable(Screen.ProfileAccountStatement.route) {
        val accountStatementViewModel: AccountStatementViewModel = hiltViewModel()

        AccountStatementRoute(
            viewModel = accountStatementViewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
