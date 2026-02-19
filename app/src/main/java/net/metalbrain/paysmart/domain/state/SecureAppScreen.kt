package net.metalbrain.paysmart.domain.state

sealed class SecureAppScreen {
    object RequireBiometricOptIn : SecureAppScreen()
    object RequirePasscodeSetup : SecureAppScreen()
    object RequireBiometricUnlock : SecureAppScreen()
    object AppContent : SecureAppScreen()
}
