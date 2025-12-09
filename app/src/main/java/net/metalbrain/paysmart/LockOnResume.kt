package net.metalbrain.paysmart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun LockOnResumeObserver(viewModel: UserViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycleScope.launch {
                    val settings = viewModel.getSecuritySettings()
                    if (settings?.passcodeEnabled == true) {
                        val due = viewModel.shouldLock(settings.lockAfterMinutes)
                        if (due) {
                            viewModel.showPasscodePrompt()
                        }
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
