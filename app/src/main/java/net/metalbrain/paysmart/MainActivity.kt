package net.metalbrain.paysmart

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.room.manager.RoomKeyManager
import net.metalbrain.paysmart.core.session.IdleSessionWatcher
import net.metalbrain.paysmart.core.session.SessionStateManager
import net.metalbrain.paysmart.data.repository.AuthSessionLogRepository
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.auth.state.SecureNavIntent
import net.metalbrain.paysmart.ui.AppNavGraph
import net.metalbrain.paysmart.ui.LocalizedAppWrapper
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var sessionStateManager: SessionStateManager

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var authSessionLogRepository: AuthSessionLogRepository


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(base))
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RoomKeyManager.ensureKeyPairExists()
        enableEdgeToEdge()
        sessionStateManager.start(lifecycleScope)

        setContent {
            val securityViewModel = hiltViewModel<SecurityViewModel>()

            val authState by userManager.authState.collectAsState(AuthState.Loading)
            val postAuthState by securityViewModel.postAuthState.collectAsState()
            val sessionState by sessionStateManager.sessionState.collectAsState()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
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

            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "unknown"
            val deferSecurityIntentForOnboarding =
                currentRoute == Screen.CreateAccount.route ||
                    currentRoute.startsWith("otp_verification/") ||
                    currentRoute.startsWith("onboarding/capabilities/") ||
                    currentRoute.startsWith("onboarding/client_information/")

            LaunchedEffect(sessionLocked, postAuthState, sessionState, currentRoute) {
                Log.d(
                    "LockStateTrace",
                    "sessionLocked=$sessionLocked postAuthState=$postAuthState sessionState=$sessionState route=$currentRoute"
                )
            }

            LaunchedEffect(postAuthState, currentRoute) {
                if (postAuthState is PostAuthState.Locked &&
                    currentRoute != Screen.RequireSessionUnlock.route
                ) {
                    Log.d(
                        "LockStateTrace",
                        "force_lock_route from=$currentRoute to=${Screen.RequireSessionUnlock.route}"
                    )
                    navController.navigate(Screen.RequireSessionUnlock.route) {
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

            PaysmartTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
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
                        LocalizedAppWrapper { AppNavGraph(navController = navController)
                            SecureApp(
                                postAuthState = postAuthState,
                                onIntent = { intent ->
                                    when (intent) {
                                        SecureNavIntent.ToStartup -> {
                                            navController.navigate(Screen.Startup.route) {
                                                popUpTo(0)
                                                launchSingleTop = true
                                            }
                                        }

                                        SecureNavIntent.ToAccountProtection -> {
                                            if (deferSecurityIntentForOnboarding) {
                                                Log.d(
                                                    "LockStateTrace",
                                                    "defer_account_protection_intent route=$currentRoute"
                                                )
                                            } else {
                                                navController.navigate(Screen.ProtectAccount.route) {
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
                                                navController.navigate(Screen.LinkFederatedAccount.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                        }

                                        SecureNavIntent.RequireSessionUnlock -> {
                                            Log.d(
                                                "LockStateTrace",
                                                "lock_intent route=$currentRoute to=${Screen.RequireSessionUnlock.route}"
                                            )
                                            navController.navigate(Screen.RequireSessionUnlock.route) {
                                                launchSingleTop = true
                                            }
                                        }

                                        SecureNavIntent.None -> Unit
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
