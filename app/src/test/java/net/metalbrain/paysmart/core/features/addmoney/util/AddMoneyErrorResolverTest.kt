package net.metalbrain.paysmart.core.features.addmoney.util

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyErrorCode
import net.metalbrain.paysmart.core.features.addmoney.repository.AddMoneyApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddMoneyErrorResolverTest {

    @Test
    fun `local flutterwave secret error becomes emulator guidance`() {
        val error = resolveAddMoneyUiError(
            throwable = AddMoneyApiException(
                statusCode = 503,
                code = AddMoneyErrorCode.MISSING_FLUTTERWAVE_SECRET_KEY,
                message = "Payments service is not configured"
            ),
            isLocal = true,
            phase = AddMoneyFailurePhase.CREATE_SESSION
        )

        assertEquals("Flutterwave emulator setup required", error.title)
        assertEquals(
            AddMoneyErrorCode.MISSING_FLUTTERWAVE_SECRET_KEY,
            error.code
        )
        assertTrue(error.isConfigurationIssue)
        assertTrue(error.supportingText.orEmpty().contains("FLUTTERWAVE_SECRET_KEY"))
    }

    @Test
    fun `generic local 503 without code becomes backend unavailable guidance`() {
        val error = resolveAddMoneyUiError(
            throwable = AddMoneyApiException(
                statusCode = 503,
                code = null,
                message = "Unable to load add money status"
            ),
            isLocal = true,
            phase = AddMoneyFailurePhase.REFRESH_STATUS
        )

        assertEquals("Local backend unavailable", error.title)
        assertTrue(error.supportingText.orEmpty().contains("Functions emulator"))
    }
}
