package net.metalbrain.paysmart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.core.security.LockGuard
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.PasscodeSetupGuard
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun SecuredApp(
    viewModel: SecurityViewModel,
    user: AuthUserModel,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    LockOnResumeObserver(viewModel)

    PasscodeSetupGuard(
        viewModel = viewModel,
        onPasscodeSet = {
            scope.launch {
                viewModel.fetchSecuritySettings()
            }
        }
    ) {
        IdleLockOverlay(viewModel = viewModel) {
            LockGuard(
                viewModel = viewModel,
                user = user

            ) {
                content()
            }
        }
    }
}
