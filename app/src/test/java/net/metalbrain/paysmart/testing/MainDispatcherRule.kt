package net.metalbrain.paysmart.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit [TestWatcher] rule that overrides the coroutine [Dispatchers.Main] with a [TestDispatcher].
 *
 * This rule allows tests to run code that uses the Main dispatcher in environments where a
 * Main Looper or UI thread is not available (e.g., local unit tests). It automatically sets
 * the dispatcher before the test starts and resets it after the test finishes.
 *
 * @property dispatcher The [TestDispatcher] to be used as the Main dispatcher. Defaults to [UnconfinedTestDispatcher].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
