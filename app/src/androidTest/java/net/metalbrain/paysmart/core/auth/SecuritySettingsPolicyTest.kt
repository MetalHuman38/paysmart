package net.metalbrain.paysmart.core.auth

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecuritySettingsPolicyTest {

    @Test
    fun getServerSecuritySettings_parsesNestedSettingsPayload() = runBlocking {
        val body = """
            {
              "ok": true,
              "settings": {
                "passcodeEnabled": true,
                "passwordEnabled": true,
                "biometricsRequired": false,
                "lockAfterMinutes": 15
              }
            }
        """.trimIndent()

        val client = fakeClient(body)
        val policy = SecuritySettingsPolicy(
            config = AuthApiConfig(baseUrl = "https://example.com"),
            httpClient = client
        )

        val result = policy.getServerSecuritySettings("fake-token")

        assertNotNull(result)
        assertEquals(true, result?.passcodeEnabled)
        assertEquals(true, result?.passwordEnabled)
        assertEquals(false, result?.biometricsRequired)
        assertEquals(15, result?.lockAfterMinutes)
    }

    @Test
    fun getServerSecuritySettings_keepsNullableFlagsAsNullWhenMissing() = runBlocking {
        val body = """
            {
              "ok": true,
              "settings": {
                "lockAfterMinutes": 10
              }
            }
        """.trimIndent()

        val client = fakeClient(body)
        val policy = SecuritySettingsPolicy(
            config = AuthApiConfig(baseUrl = "https://example.com"),
            httpClient = client
        )

        val result = policy.getServerSecuritySettings("fake-token")

        assertNotNull(result)
        assertNull(result?.passcodeEnabled)
        assertNull(result?.passwordEnabled)
        assertEquals(10, result?.lockAfterMinutes)
    }

    private fun fakeClient(body: String, statusCode: Int = 200): OkHttpClient {
        val interceptor = Interceptor { chain ->
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("OK")
                .body(body.toResponseBody())
                .build()
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }
}
