package net.metalbrain.paysmart.domain.auth.state

import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

sealed interface LocalSecurityState {

    data object Loading : LocalSecurityState

    data class Ready(
        val settings: LocalSecuritySettingsModel
    ) : LocalSecurityState
}
