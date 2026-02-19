package net.metalbrain.paysmart.domain.state

sealed class StartupNavState {
    object Splash : StartupNavState()
    object RequireAuth : StartupNavState()

    object ProtectAccount: StartupNavState()

    object RequirePassword : StartupNavState()
    
    object RequirePasscode : StartupNavState()
    object RequireBiometricOptIn : StartupNavState()
    object RequireSessionUnlock : StartupNavState()
    object RequirePasswordSetup: StartupNavState()

    object App : StartupNavState()
}
