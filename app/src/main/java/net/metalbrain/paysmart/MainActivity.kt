package net.metalbrain.paysmart

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
import net.metalbrain.paysmart.core.features.theme.viewmodel.AppThemeViewModel
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.core.session.IdleSessionWatcher
import net.metalbrain.paysmart.core.session.SessionStateManager
import net.metalbrain.paysmart.core.service.update.UpdateAppState
import net.metalbrain.paysmart.core.service.update.UpdateCoordinator
import net.metalbrain.paysmart.data.repository.AuthSessionLogRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.auth.state.SecureNavIntent
import net.metalbrain.paysmart.room.manager.RoomKeyManager
import net.metalbrain.paysmart.ui.AppNavGraph
import net.metalbrain.paysmart.ui.LocalizedAppWrapper
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.navigateClearingBackStackSafely
import net.metalbrain.paysmart.ui.navigateSafely
import net.metalbrain.paysmart.ui.network.rememberIsInternetAvailable
import net.metalbrain.paysmart.ui.screens.NoConnectionGateScreen
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaySmartAppBackground
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

/**
 * The main entry point for the PaySmart application.
 *
 * This activity serves as the primary host for the application's Compose UI and is responsible for:
 * - Initializing core system components such as the [SessionStateManager] and [RoomKeyManager].
 * - Managing application-wide state including authentication, theme, and localization.
 * - Implementing security logic such as session locking, idle timeouts, and navigation redirects based on account security status.
 * - Handling global connectivity states and displaying the no-connection gate when offline.
 * - Hosting the primary [AppNavGraph] and managing top-level navigation intents.
 *
 * It inherits from [FragmentActivity] to provide compatibility for features like Biometric authentication.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionStateManager: SessionStateManager

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var authSessionLogRepository: AuthSessionLogRepository

    @Inject
    lateinit var updateCoordinator: UpdateCoordinator

    private val inAppUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        updateCoordinator.onActivityResult(result.resultCode)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(base))
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        updateCoordinator.startObserving()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            updateCoordinator.onAppForegrounded(inAppUpdateLauncher)
        }
    }

    override fun onStop() {
        updateCoordinator.stopObserving()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RoomKeyManager.ensureKeyPairExists()
        enableEdgeToEdge()
        sessionStateManager.start(lifecycleScope)

        setContent {
            val securityViewModel = hiltViewModel<SecurityViewModel>()
            val appThemeViewModel = hiltViewModel<AppThemeViewModel>()

            val authState by userManager.authState.collectAsState(AuthState.Loading)
            val postAuthState by securityViewModel.postAuthState.collectAsState()
            val sessionState by sessionStateManager.sessionState.collectAsState()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val themeMode by appThemeViewModel.themeMode.collectAsState()
            val isOnline = rememberIsInternetAvailable()
            var showNoConnectionGate by rememberSaveable { mutableStateOf(!isOnline) }
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }
            val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
            val sessionLocked = localSettings?.sessionLocked
            val hasUnlockMethod = localSettings?.let {
                it.biometricsEnabled || it.passcodeEnabled || it.passwordEnabled
            } ?: false
            val passwordReady = localSettings?.let {
                it.passwordEnabled && it.localPasswordSetAt != null
            } == true
            val lockAfterMinutes = (localSettings?.lockAfterMinutes ?: 5).coerceAtLeast(1)
            val idleLockEnabled =
                authState is AuthState.Authenticated &&
                        postAuthState is PostAuthState.Ready &&
                        hasUnlockMethod
            val updateUiState by updateCoordinator.uiState.collectAsState()
            val updateSnackbarHostState = remember { SnackbarHostState() }

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "unknown"
            val updateAppState = resolveUpdateAppState(
                currentRoute = currentRoute,
                showNoConnectionGate = showNoConnectionGate,
            )
            val deferSecurityIntentForOnboarding =
                currentRoute == Screen.CreateAccount.route ||
                    currentRoute.startsWith("otp_verification/") ||
                    currentRoute.startsWith("onboarding/capabilities/") ||
                    currentRoute.startsWith("onboarding/security_steps/") ||
                    currentRoute.startsWith("onboarding/client_information/") ||
                    currentRoute.startsWith("onboarding/mfa_nudge/") ||
                    currentRoute.startsWith(Screen.ProtectAccount.route) ||
                    currentRoute.startsWith(Screen.BiometricOptIn.route) ||
                    currentRoute.startsWith(Screen.SetUpPassCode.route) ||
                    currentRoute.startsWith(Screen.ProfileChangePasscodeGate.route) ||
                    currentRoute.startsWith(Screen.ProfileChangePasscode.route) ||
                    currentRoute.startsWith(Screen.PasskeySetup.route) ||
                    currentRoute.startsWith(Screen.LinkFederatedAccount.route) ||
                    currentRoute.startsWith(Screen.AddEmail.route) ||
                    currentRoute.startsWith("email_sent/") ||
                    currentRoute.startsWith(Screen.EmailVerified.route)
            val shouldDeferPasswordFallback =
                deferSecurityIntentForOnboarding ||
                        currentRoute == Screen.Splash.route ||
                        currentRoute == Screen.Startup.route ||
                        currentRoute == Screen.Login.route ||
                        currentRoute.startsWith("language")

            LaunchedEffect(sessionLocked, postAuthState, sessionState, currentRoute) {
                Log.d(
                    "LockStateTrace",
                    "sessionLocked=$sessionLocked postAuthState=$postAuthState sessionState=$sessionState route=$currentRoute"
                )
            }

            LaunchedEffect(isOnline, currentRoute) {
                Log.d("ConnectivityGate", "isOnline=$isOnline route=$currentRoute")
            }

            LaunchedEffect(isOnline) {
                if (!isOnline) {
                    showNoConnectionGate = true
                }
            }

            LaunchedEffect(updateAppState) {
                updateCoordinator.onAppStateChanged(updateAppState)
                if (updateAppState == UpdateAppState.SAFE) {
                    updateCoordinator.onAppForegrounded(inAppUpdateLauncher)
                }
            }

            LaunchedEffect(postAuthState, currentRoute, showNoConnectionGate) {
                if (showNoConnectionGate) {
                    return@LaunchedEffect
                }
                if (postAuthState is PostAuthState.Locked &&
                    currentRoute != Screen.RequireSessionUnlock.route
                ) {
                    Log.d(
                        "LockStateTrace",
                        "force_lock_route from=$currentRoute to=${Screen.RequireSessionUnlock.route}"
                    )
                    navController.navigateSafely(
                        route = Screen.RequireSessionUnlock.route,
                        currentRoute = currentRoute,
                        source = "post_auth_lock_redirect",
                    ) {
                        launchSingleTop = true
                    }
                }
            }

            LaunchedEffect(
                authState,
                passwordReady,
                shouldDeferPasswordFallback,
                currentRoute,
                isOnline,
                showNoConnectionGate,
            ) {
                val authenticated = authState is AuthState.Authenticated
                if (!authenticated || !isOnline || shouldDeferPasswordFallback || showNoConnectionGate) {
                    return@LaunchedEffect
                }
                if (!passwordReady &&
                    currentRoute != Screen.CreatePassword.route &&
                    currentRoute != Screen.RequireSessionUnlock.route
                ) {
                    Log.d(
                        "LockStateTrace",
                        "fallback_create_password_redirect route=$currentRoute"
                    )
                    navController.navigateSafely(
                        route = Screen.CreatePassword.route,
                        currentRoute = currentRoute,
                        source = "password_fallback_redirect",
                    ) {
                        launchSingleTop = true
                    }
                }
            }

            val authenticatedUid = (authState as? AuthState.Authenticated)?.uid
            LaunchedEffect(authenticatedUid) {
                if (authenticatedUid == null) {
                    Log.d("MainActivity", "roomSessionLog: null (unauthenticated)")
                    return@LaunchedEffect
                }
                val roomSession = authSessionLogRepository.getLatestSessionLog(authenticatedUid)
                val shape = roomSession?.let {
                    "sid=${it.sid}, userId=${it.userId}, sv=${it.sessionVersion}, ts=${it.signInAtSeconds}, recordedAt=${it.recordedAt}"
                } ?: "null"
                Log.d("MainActivity", "roomSessionLog: $shape")
            }

            val updateDownloadedMessage = stringResource(R.string.app_update_downloaded_message)
            val updateRestartAction = stringResource(R.string.app_update_restart_action)
            LaunchedEffect(
                updateUiState.showRestartPrompt,
                updateUiState.downloadedVersionCode,
                updateDownloadedMessage,
                updateRestartAction,
            ) {
                if (!updateUiState.showRestartPrompt) {
                    updateSnackbarHostState.currentSnackbarData?.dismiss()
                    return@LaunchedEffect
                }

                val result = updateSnackbarHostState.showSnackbar(
                    message = updateDownloadedMessage,
                    actionLabel = updateRestartAction,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    updateCoordinator.completeUpdate()
                }
                updateCoordinator.acknowledgeRestartPrompt()
            }

            PaysmartTheme(darkTheme = isDarkTheme) {
                LocalizedAppWrapper {
                    PaySmartAppBackground {
                        Surface(color = Color.Transparent) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                IdleSessionWatcher(
                                    enabled = idleLockEnabled,
                                    lockAfterMinutes = lockAfterMinutes,
                                    onTimeout = {
                                        Log.d(
                                            "LockStateTrace",
                                            "idle_timeout sessionLockedBefore=$sessionLocked postAuthState=$postAuthState route=$currentRoute"
                                        )
                                        lifecycleScope.launch {
                                            sessionStateManager.lockSession()
                                        }
                                    },
                                    onInteraction = {
                                        securityViewModel.registerInteractionHeartbeat()
                                    }
                                ) {
                                    AppNavGraph(navController = navController)
                                    if (!showNoConnectionGate) {
                                        SecureApp(
                                            postAuthState = postAuthState,
                                            onIntent = { intent ->
                                                when (intent) {
                                                    SecureNavIntent.ToStartup -> {
                                                        navController.navigateClearingBackStackSafely(
                                                            route = Screen.Startup.route,
                                                            currentRoute = currentRoute,
                                                            source = "secure_intent_startup",
                                                        )
                                                    }

                                                    SecureNavIntent.ToAccountProtection -> {
                                                        if (deferSecurityIntentForOnboarding) {
                                                            Log.d(
                                                                "LockStateTrace",
                                                                "defer_account_protection_intent route=$currentRoute"
                                                            )
                                                        } else {
                                                            navController.navigateSafely(
                                                                route = Screen.ProtectAccount.route,
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_account_protection",
                                                            ) {
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    }

                                                    SecureNavIntent.ToCreatePassword -> {
                                                        if (deferSecurityIntentForOnboarding) {
                                                            Log.d(
                                                                "LockStateTrace",
                                                                "defer_create_password_intent route=$currentRoute"
                                                            )
                                                        } else if (currentRoute != Screen.CreatePassword.route) {
                                                            navController.navigateSafely(
                                                                route = Screen.CreatePassword.route,
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_create_password",
                                                            ) {
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    }

                                                    SecureNavIntent.ToEmailVerification -> {
                                                        if (deferSecurityIntentForOnboarding) {
                                                            Log.d(
                                                                "LockStateTrace",
                                                                "defer_email_verification_intent route=$currentRoute"
                                                            )
                                                        } else {
                                                            navController.navigateSafely(
                                                                route = Screen.AddEmail.route,
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_email_verification",
                                                            ) {
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    }

                                                    SecureNavIntent.RequireSessionUnlock -> {
                                                        Log.d(
                                                            "LockStateTrace",
                                                            "lock_intent route=$currentRoute to=${Screen.RequireSessionUnlock.route}"
                                                        )
                                                        navController.navigateSafely(
                                                            route = Screen.RequireSessionUnlock.route,
                                                            currentRoute = currentRoute,
                                                            source = "secure_intent_session_unlock",
                                                        ) {
                                                            launchSingleTop = true
                                                        }
                                                    }

                                                    SecureNavIntent.None -> Unit
                                                }
                                            }
                                        )
                                    }
                                    if (showNoConnectionGate) {
                                        NoConnectionGateScreen(
                                            isOnline = isOnline,
                                            onReturnToLogin = {
                                                if (!isOnline) return@NoConnectionGateScreen
                                                showNoConnectionGate = false
                                                navController.navigateClearingBackStackSafely(
                                                    route = Screen.Login.route,
                                                    currentRoute = currentRoute,
                                                    source = "connectivity_return_to_login",
                                                )
                                            }
                                        )
                                    }
                                }
                                SnackbarHost(
                                    hostState = updateSnackbarHostState,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(Dimens.screenPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun resolveUpdateAppState(
    currentRoute: String,
    showNoConnectionGate: Boolean,
): UpdateAppState {
    if (showNoConnectionGate) {
        return UpdateAppState.CRITICAL
    }
    if (isCriticalUpdateRoute(currentRoute)) {
        return UpdateAppState.CRITICAL
    }
    if (currentRoute == "unknown") {
        return UpdateAppState.UNKNOWN
    }
    return UpdateAppState.SAFE
}

private fun isCriticalUpdateRoute(route: String): Boolean {
    return route.startsWith("otp_verification/") ||
        route.startsWith(Screen.Reauthenticate.baseRoute) ||
        route.startsWith(Screen.SetUpPassCode.route) ||
        route.startsWith(Screen.ProfileChangePasscodeGate.route) ||
        route.startsWith(Screen.ProfileChangePasscode.route) ||
        route.startsWith(Screen.VerifyPasscode.route) ||
        route.startsWith(Screen.RequireSessionUnlock.route) ||
        route.startsWith(Screen.AddMoney.route) ||
        route.startsWith(Screen.FundingAccount.route) ||
        route.startsWith(Screen.TransactionDetail.route.substringBefore("/{")) ||
        route.startsWith(Screen.Transactions.route) ||
        route.startsWith(Screen.InvoiceFlow.route) ||
        route.startsWith("invoice/") ||
        route.startsWith(Screen.ProfileIdentity.route) ||
        route.startsWith(Screen.OnboardingProfile.route) ||
        route.startsWith(Screen.ProfileIdentityResolver.route)
}
