package net.metalbrain.paysmart.core.features.account.security.viewmodel

import com.google.firebase.Timestamp
import net.metalbrain.paysmart.core.session.SessionState
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.domain.auth.state.PostAuthState
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.domain.model.SecuritySettingsModel
import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityViewModelPostAuthStateTest {

    @Test
    fun `requires recovery method when no recovery signal exists`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(),
            cloudState = null,
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.RequireRecoveryMethod, result)
    }

    @Test
    fun `requires recovery method when passcode is set but no email or password recovery signal`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(passcodeEnabled = true),
            cloudState = null,
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.RequireRecoveryMethod, result)
    }

    @Test
    fun `requires recovery password when email is verified but no local password yet`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(hasVerifiedEmail = true),
            cloudState = null,
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.RequireRecoveryPassword, result)
    }

    @Test
    fun `requires password recovery when account password exists remotely but local password is gone`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(passwordEnabled = false, localPasswordSetAt = null),
            cloudState = SecuritySettingsModel(passwordEnabled = true),
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.RequirePasswordRecovery, result)
    }

    @Test
    fun `returns locked when password is ready but session is locked`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(
                passwordEnabled = true,
                localPasswordSetAt = Timestamp.now()
            ),
            cloudState = SecuritySettingsModel(passwordEnabled = true),
            sessionState = SessionState.Locked
        )

        assertEquals(PostAuthState.Locked, result)
    }

    @Test
    fun `returns ready when recovery method and local password are both set`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(
                passwordEnabled = true,
                localPasswordSetAt = Timestamp.now()
            ),
            cloudState = SecuritySettingsModel(passwordEnabled = true),
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.Ready, result)
    }

    @Test
    fun `returns ready when recovery method is explicitly marked ready and password is set`() {
        val result = resolvePostAuthState(
            authState = AuthState.Authenticated(uid = "user-1"),
            localState = LocalSecuritySettingsModel(
                recoveryMethodReady = true,
                passwordEnabled = true,
                localPasswordSetAt = Timestamp.now()
            ),
            cloudState = null,
            sessionState = SessionState.Unlocked
        )

        assertEquals(PostAuthState.Ready, result)
    }
}
