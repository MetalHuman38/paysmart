package net.metalbrain.paysmart.domain.state;

sealed class StartupNavState {
    object Splash : StartupNavState()
    object RequireAuth : StartupNavState()
    object RequirePasscode : StartupNavState()

    object RequirePasswordSetup: StartupNavState()
    object App : StartupNavState()
}
