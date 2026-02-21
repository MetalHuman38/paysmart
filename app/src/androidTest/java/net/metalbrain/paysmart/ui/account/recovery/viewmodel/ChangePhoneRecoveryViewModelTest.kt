package net.metalbrain.paysmart.ui.account.recovery.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.metalbrain.paysmart.core.auth.PhoneChangePolicyHandler
import net.metalbrain.paysmart.data.repository.UserProfileRepository
import net.metalbrain.paysmart.ui.account.recovery.auth.ChangePhoneRecoveryAuthGateway
import net.metalbrain.paysmart.ui.account.recovery.auth.data.PhoneRecoverySession
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangePhoneRecoveryViewModelTest {

    @Test
    fun confirmCode_success_setsSuccessAndUpdatesProfile() = runBlocking {
        val userRepo = mockk<UserProfileRepository>()
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
        coEvery { userRepo.updatePhoneNumber(any(), any()) } just Runs

        val viewModel = ChangePhoneRecoveryViewModel(userRepo, policyHandler, gateway)
        viewModel.onPhoneNumberChanged("+447988777954")
        viewModel.sendCode(activity)
        viewModel.onOtpChanged("123456")
        viewModel.confirmCode()

        waitUntil {
            val state = viewModel.uiState.value
            state.isSuccess || !state.error.isNullOrBlank()
        }

        val finalState = viewModel.uiState.value
        assertTrue("Expected success, but state=$finalState", finalState.isSuccess)
        assertNull(finalState.error)
        coVerify(exactly = 1) {
            policyHandler.confirmPhoneChanged("token-success", "+447988777954")
        }
        coVerify(exactly = 1) {
            userRepo.updatePhoneNumber("uid-success", "+447988777954")
        }
    }

    @Test
    fun confirmCode_failure_whenServerRejects_setsErrorAndSkipsLocalProfileUpdate() = runBlocking {
        val userRepo = mockk<UserProfileRepository>()
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
        coEvery { userRepo.updatePhoneNumber(any(), any()) } just Runs

        val viewModel = ChangePhoneRecoveryViewModel(userRepo, policyHandler, gateway)
        viewModel.onPhoneNumberChanged("+447911111111")
        viewModel.sendCode(activity)
        viewModel.onOtpChanged("123456")
        viewModel.confirmCode()

        waitUntil {
            val state = viewModel.uiState.value
            state.isSuccess || !state.error.isNullOrBlank()
        }

        val finalState = viewModel.uiState.value
        assertFalse("Expected failure, but state=$finalState", finalState.isSuccess)
        assertTrue(
            finalState.error?.contains(
                "Server failed to confirm phone number change",
                ignoreCase = true
            ) == true
        )
        coVerify(exactly = 1) {
            policyHandler.confirmPhoneChanged("token-failure", "+447911111111")
        }
        coVerify(exactly = 0) {
            userRepo.updatePhoneNumber(any(), any())
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
