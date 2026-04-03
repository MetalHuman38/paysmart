package net.metalbrain.paysmart.ui.home.data

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.ui.home.state.HomeRecentRecipient

interface HomeRecentRecipientGateway {
    fun observeRecentByUserId(userId: String): Flow<List<HomeRecentRecipient>>
}
