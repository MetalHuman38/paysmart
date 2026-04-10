package net.metalbrain.paysmart

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.Manifest
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
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode
import net.metalbrain.paysmart.core.features.theme.viewmodel.AppThemeViewModel
import net.metalbrain.paysmart.core.common.runtime.AppVersionInfo
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.core.notifications.NotificationBootstrapper
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
import net.metalbrain.paysmart.navigator.AppNavGraph
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.navigator.navigateClearingBackStackSafely
import net.metalbrain.paysmart.navigator.navigateSafely
import net.metalbrain.paysmart.ui.network.rememberIsInternetAvailable
import net.metalbrain.paysmart.ui.screens.loader.AppLoadingScreen
import net.metalbrain.paysmart.ui.screens.loader.LoadingPhase
import net.metalbrain.paysmart.ui.viewmodel.AppLoadingViewModel
import net.metalbrain.paysmart.ui.screens.NoConnectionGateScreen
import net.metalbrain.paysmart.ui.screens.loader.rememberStabilizedLoading
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaySmartAppBackground
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import net.metalbrain.paysmart.ui.version.ProvideAppVersionInfo

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

    @Inject
    lateinit var notificationBootstrapper: NotificationBootstrapper

    @Inject
    lateinit var appVersionInfo: AppVersionInfo

    private val inAppUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        updateCoordinator.onActivityResult(result.resultCode)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationBootstrapper.onNotificationPermissionResult()
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
        notificationBootstrapper.syncRegistrationIfPossible()
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
        notificationBootstrapper.start()

        setContent {
            val securityViewModel = hiltViewModel<SecurityViewModel>()
            val appThemeViewModel = hiltViewModel<AppThemeViewModel>()
            val appLoadingViewModel = hiltViewModel<AppLoadingViewModel>()

            val authState by userManager.authState.collectAsState(AuthState.Loading)
            val postAuthState by securityViewModel.postAuthState.collectAsState()
            val sessionState by sessionStateManager.sessionState.collectAsState()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val themeMode by appThemeViewModel.themeMode.collectAsState()
            val themeVariant by appThemeViewModel.themeVariant.collectAsState()
            val loadingPhase by appLoadingViewModel.loadingPhase.collectAsState()
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
            val rootLoadingPhase = resolveRootLoadingPhase(
                authState = authState,
                postAuthState = postAuthState,
                localSecurityState = localSecurityState,
                showNoConnectionGate = showNoConnectionGate,
            )
            val startDestination = resolveRootStartDestination(postAuthState)
            val updateAppState = resolveUpdateAppState(
                currentRoute = currentRoute,
                showNoConnectionGate = showNoConnectionGate,
            )
            val deferSecurityIntentForOnboarding =
                currentRoute == "unknown" ||
                    currentRoute == Screen.Splash.route ||
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
                    currentRoute.startsWith(Screen.CreatePassword.BASEROUTE) ||
                    currentRoute.startsWith(Screen.AddEmail.route) ||
                    currentRoute.startsWith("email_sent/") ||
                    currentRoute.startsWith(Screen.EmailVerified.route)

            LaunchedEffect(
                loadingPhase,
                rootLoadingPhase,
                authState,
                postAuthState,
                localSecurityState,
                currentRoute,
                startDestination,
                showNoConnectionGate,
                deferSecurityIntentForOnboarding,
            ) {
                Log.d(
                    "RootLoadingTrace",
                    "loadingPhase=$loadingPhase rootLoadingPhase=$rootLoadingPhase " +
                        "auth=${authState::class.simpleName} post=$postAuthState " +
                        "local=${localSecurityState::class.simpleName} route=$currentRoute " +
                        "start=$startDestination noConnection=$showNoConnectionGate " +
                        "defer=$deferSecurityIntentForOnboarding"
                )
            }

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

            LaunchedEffect(rootLoadingPhase) {
                if (rootLoadingPhase == LoadingPhase.Idle) {
                    appLoadingViewModel.complete()
                } else {
                    appLoadingViewModel.setPhase(rootLoadingPhase)
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

            LaunchedEffect(authenticatedUid, currentRoute, showNoConnectionGate) {
                if (authenticatedUid == null || showNoConnectionGate) {
                    return@LaunchedEffect
                }

                if (
                    currentRoute == Screen.Home.route &&
                    notificationBootstrapper.shouldRequestNotificationPermission()
                ) {
                    notificationBootstrapper.markPermissionPromptShown()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    notificationBootstrapper.syncRegistrationIfPossible()
                }
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
            }

            PaysmartTheme(
                darkTheme = isDarkTheme,
                themeVariant = themeVariant
            ) {
                ProvideAppVersionInfo(appVersionInfo = appVersionInfo) {
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
                                        AppRoot(
                                            navController = navController,
                                            startDestination = startDestination,
                                            loadingPhase = loadingPhase,
                                            showNoConnectionGate = showNoConnectionGate,
                                            isOnline = isOnline,
                                            postAuthState = postAuthState,
                                            onReturnToLogin = {
                                                if (isOnline) {
                                                    showNoConnectionGate = false
                                                    navController.navigateClearingBackStackSafely(
                                                        route = Screen.Login.route,
                                                        currentRoute = currentRoute,
                                                        source = "connectivity_return_to_login",
                                                    )
                                                }
                                            },
                                            onIntent = { intent ->
                                                when (intent) {
                                                    SecureNavIntent.ToStartup -> {
                                                        navController.navigateClearingBackStackSafely(
                                                            route = Screen.Startup.route,
                                                            currentRoute = currentRoute,
                                                            source = "secure_intent_startup",
                                                        )
                                                    }

                                                    SecureNavIntent.ToRecoveryMethod -> {
                                                        if (deferSecurityIntentForOnboarding) {
                                                            Log.d(
                                                                "LockStateTrace",
                                                                "defer_recovery_method_intent route=$currentRoute"
                                                            )
                                                        } else {
                                                            navController.navigateSafely(
                                                                route = Screen.LinkFederatedAccount.route,
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_recovery_method",
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
                                                                route = Screen.CreatePassword.BASEROUTE,
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_create_password",
                                                            ) {
                                                                launchSingleTop = true
                                                            }
                                                        }
                                                    }

                                                    SecureNavIntent.ToPasswordRecovery -> {
                                                        if (deferSecurityIntentForOnboarding) {
                                                            Log.d(
                                                                "LockStateTrace",
                                                                "defer_password_recovery_intent route=$currentRoute"
                                                            )
                                                        } else if (
                                                            currentRoute != Screen.Reauthenticate.route &&
                                                            currentRoute != Screen.CreatePassword.route
                                                        ) {
                                                            navController.navigateSafely(
                                                                route = Screen.Reauthenticate.routeWithTarget(
                                                                    Screen.CreatePassword.BASEROUTE
                                                                ),
                                                                currentRoute = currentRoute,
                                                                source = "secure_intent_password_recovery",
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

@Composable
private fun AppRoot(
    navController: NavHostController,
    startDestination: String,
    loadingPhase: LoadingPhase,
    showNoConnectionGate: Boolean,
    isOnline: Boolean,
    postAuthState: PostAuthState,
    onReturnToLogin: () -> Unit,
    onIntent: (SecureNavIntent) -> Unit,
) {
    val showLoading = rememberStabilizedLoading(phase = loadingPhase)
    val initialStartDestination = rememberSaveable { startDestination }

    LaunchedEffect(
        showLoading,
        loadingPhase,
        startDestination,
        initialStartDestination,
        showNoConnectionGate,
        postAuthState,
    ) {
        Log.d(
            "RootLoadingTrace",
            "app_root showLoading=$showLoading " +
                "loadingPhase=$loadingPhase start=$startDestination " +
                "initialStart=$initialStartDestination " +
                "noConnection=$showNoConnectionGate post=$postAuthState"
        )
    }

    when {
        showNoConnectionGate -> {
            NoConnectionGateScreen(
                isOnline = isOnline,
                onReturnToLogin = onReturnToLogin,
            )
        }

        else -> {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavGraph(
                    navController = navController,
                    startDestination = initialStartDestination,
                )
                SecureApp(
                    postAuthState = postAuthState,
                    onIntent = onIntent,
                )

                if (showLoading) {
                    AppLoadingScreen(phase = loadingPhase)
                }
            }
        }
    }
}

private fun resolveRootLoadingPhase(
    authState: AuthState,
    postAuthState: PostAuthState,
    localSecurityState: LocalSecurityState,
    showNoConnectionGate: Boolean,
): LoadingPhase {
    if (showNoConnectionGate) {
        return LoadingPhase.Idle
    }

    return when {
        authState is AuthState.Loading -> LoadingPhase.Startup
        postAuthState is PostAuthState.Loading -> LoadingPhase.Authentication
        authState is AuthState.Authenticated && localSecurityState is LocalSecurityState.Loading ->
            LoadingPhase.FetchingData

        else -> LoadingPhase.Idle
    }
}

private fun resolveRootStartDestination(postAuthState: PostAuthState): String {
    return when (postAuthState) {
        PostAuthState.Loading -> Screen.Startup.route
        PostAuthState.Unauthenticated -> Screen.Startup.route
        PostAuthState.RequireRecoveryMethod -> Screen.LinkFederatedAccount.route
        PostAuthState.RequireRecoveryPassword -> Screen.CreatePassword.BASEROUTE
        PostAuthState.RequirePasswordRecovery -> Screen.Reauthenticate.routeWithTarget(
            Screen.CreatePassword.BASEROUTE
        )
        PostAuthState.Locked -> Screen.RequireSessionUnlock.route
        PostAuthState.Ready -> Screen.Home.route
    }
}

private fun isCriticalUpdateRoute(route: String): Boolean {
    return route.startsWith("otp_verification/") ||
        route.startsWith(Screen.Reauthenticate.BASEROUTES) ||
        route.startsWith(Screen.LoginMfaChallenge.route) ||
        route.startsWith(Screen.SetUpPassCode.route) ||
        route.startsWith(Screen.ProfileChangePasscodeGate.route) ||
        route.startsWith(Screen.ProfileChangePasscode.route) ||
        route.startsWith(Screen.VerifyPasscode.route) ||
        route.startsWith(Screen.RequireSessionUnlock.route) ||
        route.startsWith(Screen.AddMoney.route) ||
        route.startsWith(Screen.FundingAccount.route) ||
        route.startsWith(Screen.UkAccount.BASEROUTE) ||
        route.startsWith(Screen.TransactionDetail.route.substringBefore("/{")) ||
        route.startsWith(Screen.Transactions.route) ||
        route.startsWith(Screen.InvoiceFlow.route) ||
        route.startsWith("invoice/") ||
        route.startsWith(Screen.ProfileIdentity.route) ||
        route.startsWith(Screen.OnboardingProfile.route) ||
        route.startsWith(Screen.ProfileIdentityResolver.route)
}
