package net.metalbrain.paysmart

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun SecuredApp(viewModel: UserViewModel, content: @Composable () -> Unit) {
    LockOnResumeObserver(viewModel)
    IdleLockOverlay(viewModel) {
        content()
    }
}
