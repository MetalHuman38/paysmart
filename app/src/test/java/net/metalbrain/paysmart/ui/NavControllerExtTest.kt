package net.metalbrain.paysmart.ui

import android.util.Log
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class NavControllerExtTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `navigateSafely navigates trimmed route when graph is ready`() {
        val navController = readyNavController()

        navController.navigateSafely(
            route = " profile ",
            currentRoute = Screen.Home.route,
            source = "test",
        )

        verify(exactly = 1) {
            navController.navigate("profile", any<NavOptionsBuilder.() -> Unit>())
        }
    }

    @Test
    fun `navigateSafely skips duplicate route when target matches current route`() {
        val navController = readyNavController()

        navController.navigateSafely(
            route = Screen.Home.route,
            currentRoute = Screen.Home.route,
            source = "test",
        )

        verify(exactly = 0) {
            navController.navigate(any<String>(), any<NavOptionsBuilder.() -> Unit>())
        }
    }

    @Test
    fun `navigateSafely skips when graph is not ready`() {
        val navController = mockk<NavHostController>(relaxed = true) {
            every { graph } throws IllegalStateException("graph not ready")
        }

        navController.navigateSafely(
            route = Screen.Login.route,
            currentRoute = Screen.Home.route,
            source = "test",
        )

        verify(exactly = 0) {
            navController.navigate(any<String>(), any<NavOptionsBuilder.() -> Unit>())
        }
    }

    @Test
    fun `navigateClearingBackStackSafely skips when graph is not ready`() {
        val navController = mockk<NavHostController>(relaxed = true) {
            every { graph } throws IllegalStateException("graph not ready")
        }

        navController.navigateClearingBackStackSafely(
            route = Screen.Startup.route,
            currentRoute = Screen.Home.route,
            source = "test",
        )

        verify(exactly = 0) {
            navController.navigate(any<String>(), any<NavOptionsBuilder.() -> Unit>())
        }
    }

    private fun readyNavController(startDestinationId: Int = 1): NavHostController {
        val startDestination = mockk<NavDestination> {
            every { id } returns startDestinationId
        }
        val graph = mockk<NavGraph> {
            every { this@mockk.startDestinationId } returns startDestinationId
            every { findNode(startDestinationId) } returns startDestination
        }

        return mockk(relaxed = true) {
            every { this@mockk.graph } returns graph
            every { navigate(any<String>(), any<NavOptionsBuilder.() -> Unit>()) } just runs
        }
    }
}
