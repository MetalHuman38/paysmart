package net.metalbrain.paysmart.core.account.recovery

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.metalbrain.paysmart.core.auth.PhoneChangePolicyHandler
import net.metalbrain.paysmart.core.features.account.recovery.auth.ChangePhoneRecoveryAuthGateway
import net.metalbrain.paysmart.core.features.account.recovery.auth.data.PhoneRecoverySession
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for [ChangePhoneRecoveryViewModel].
 *
 * This test class verifies the business logic for updating a user's phone number,
 * ensuring that the UI state updates correctly and that the local profile is only
 * updated if the backend policy handler confirms the change.
 *
 * It uses a [FakeGateway] to simulate Firebase Phone Authentication and mocks for
 * [UserProfileRepository] and [PhoneChangePolicyHandler] to validate interactions.
 */
@RunWith(AndroidJUnit4::class)
class ChangePhoneRecoveryViewModelTest {

    @Test
    fun confirmCode_success_setsSuccess() = runBlocking {
        val policyHandler = mockk<PhoneChangePolicyHandler>()
        val gateway = FakeGateway(
            session = PhoneRecoverySession(
                uid = "uid-success",
                phoneNumber = "+447988777954",
                idToken = "token-success"
            )
        )
        val activity = mockk<FragmentActivity>(relaxed = true)

        coEvery { policyHandler.confirmPhoneChanged(any(), any()) } returns true

        val viewModel = ChangePhoneRecoveryViewModel(policyHandler, gateway)
        viewModel.onPhoneNumberChanged("+447988777954")
        viewModel.sendCode(activity)
        viewModel.onOtpChanged("123456")
        viewModel.confirmCode()

        waitUntil {
            val state = viewModel.uiState.value
            state.isSuccess || !state.error.isNullOrBlank()
        }

        val finalState = viewModel.uiState.value
        Assert.assertTrue("Expected success, but state=$finalState", finalState.isSuccess)
        Assert.assertNull(finalState.error)
        coVerify(exactly = 1) {
            policyHandler.confirmPhoneChanged("token-success", "+447988777954")
        }
    }

    @Test
    fun confirmCode_failure_whenServerRejects_setsError() = runBlocking {
        val policyHandler = mockk<PhoneChangePolicyHandler>()
        val gateway = FakeGateway(
            session = PhoneRecoverySession(
                uid = "uid-failure",
                phoneNumber = "+447911111111",
                idToken = "token-failure"
            )
        )
        val activity = mockk<FragmentActivity>(relaxed = true)

        coEvery { policyHandler.confirmPhoneChanged(any(), any()) } returns false

        val viewModel = ChangePhoneRecoveryViewModel(policyHandler, gateway)
        viewModel.onPhoneNumberChanged("+447911111111")
        viewModel.sendCode(activity)
        viewModel.onOtpChanged("123456")
        viewModel.confirmCode()

        waitUntil {
            val state = viewModel.uiState.value
            state.isSuccess || !state.error.isNullOrBlank()
        }

        val finalState = viewModel.uiState.value
        Assert.assertFalse("Expected failure, but state=$finalState", finalState.isSuccess)
        Assert.assertTrue(
            finalState.error?.contains(
                "Server failed to confirm phone number change",
                ignoreCase = true
            ) == true
        )
        coVerify(exactly = 1) {
            policyHandler.confirmPhoneChanged("token-failure", "+447911111111")
        }
    }

    private suspend fun waitUntil(
        timeoutMs: Long = 2_000,
        predicate: () -> Boolean
    ) {
        withTimeout(timeoutMs) {
            while (!predicate()) {
                delay(20)
            }
        }
    }

    private class FakeGateway(
        private val session: PhoneRecoverySession
    ) : ChangePhoneRecoveryAuthGateway {
        private val credential = mockk<PhoneAuthCredential>(relaxed = true)

        override fun hasAuthenticatedUser(): Boolean = true

        override fun startPhoneVerification(
            activity: FragmentActivity,
            phoneNumber: String,
            resendToken: PhoneAuthProvider.ForceResendingToken?,
            callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
        ) {
            callbacks.onCodeSent(
                "verification-id",
                mockk<PhoneAuthProvider.ForceResendingToken>(relaxed = true)
            )
        }

        override fun credentialFromCode(verificationId: String, code: String): PhoneAuthCredential {
            return credential
        }

        override suspend fun applyPhoneCredential(
            credential: PhoneAuthCredential,
            fallbackPhoneE164: String?
        ): PhoneRecoverySession = session
    }
}
