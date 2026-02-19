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
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.core.security.RoomKeyManager
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
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

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
            Log.d("MainActivity", "authState: $authState")
            val postAuthState by securityViewModel.postAuthState.collectAsState()
            Log.d("MainActivity", "postAuthState: $postAuthState")
            val sessionState by sessionStateManager.sessionState.collectAsState()
            Log.d("MainActivity", "sessionState: $sessionState")
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            Log.d("MainActivity", "localSecurityState: $localSecurityState")
            val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
            val hasUnlockMethod = localSettings?.let {
                it.biometricsEnabled || it.passcodeEnabled || it.passwordEnabled
            } ?: false
            val lockAfterMinutes = (localSettings?.lockAfterMinutes ?: 5).coerceAtLeast(1)
            val idleLockEnabled =
                authState is AuthState.Authenticated &&
                    postAuthState is PostAuthState.Ready &&
                    hasUnlockMethod

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

            val navController = rememberNavController()

            PaysmartTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    IdleSessionWatcher(
                        enabled = idleLockEnabled,
                        lockAfterMinutes = lockAfterMinutes,
                        onTimeout = {
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
                                            navController.navigate(Screen.ProtectAccount.route) {
                                                launchSingleTop = true
                                            }
                                        }

                                        SecureNavIntent.ToEmailVerification -> {
                                            navController.navigate(Screen.LinkFederatedAccount.route) {
                                                launchSingleTop = true
                                            }
                                        }

                                        SecureNavIntent.RequireSessionUnlock -> {
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
