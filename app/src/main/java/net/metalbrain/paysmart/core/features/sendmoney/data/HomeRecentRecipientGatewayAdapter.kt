package net.metalbrain.paysmart.core.features.sendmoney.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.ui.home.data.HomeRecentRecipientGateway
import net.metalbrain.paysmart.ui.home.state.HomeRecentRecipient

@Singleton
class HomeRecentRecipientGatewayAdapter @Inject constructor(
    private val recentSendRecipientRepository: RecentSendRecipientRepository,
) : HomeRecentRecipientGateway {

    override fun observeRecentByUserId(userId: String): Flow<List<HomeRecentRecipient>> {
        return recentSendRecipientRepository.observeRecentByUserId(userId)
            .map { recipients ->
                recipients.map { recipient ->
                    HomeRecentRecipient(
                        recipientKey = recipient.recipientKey,
                        displayName = recipient.displayName,
                        subtitle = recipient.subtitle,
                        targetCurrencyCode = recipient.targetCurrency,
                    )
                }
            }
    }
}
