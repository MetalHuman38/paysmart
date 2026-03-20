package net.metalbrain.paysmart.core.features.capabilities.mapper

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitMarketProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitProfile
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitSectionType
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitTabSpec
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitValueProfile
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitCardUiState

internal object AccountLimitDetailsUiMapper {

    fun resolveTabs(profile: AccountLimitProfile): List<AccountLimitTabSpec> {
        return profile.tabs
            .distinctBy { it.key }
            .ifEmpty { AccountLimitCatalog.defaultTabs() }
    }

    fun resolveSelectedTab(
        tabs: List<AccountLimitTabSpec>,
        selectedTab: AccountLimitKey
    ): AccountLimitKey {
        return if (tabs.any { it.key == selectedTab }) {
            selectedTab
        } else {
            tabs.firstOrNull()?.key ?: AccountLimitKey.SEND
        }
    }

    fun buildCards(
        profile: AccountLimitProfile,
        marketProfile: AccountLimitMarketProfile,
        selectedTab: AccountLimitKey,
        valueProfile: AccountLimitValueProfile,
        spentOfLimitFormat: (String, String) -> String,
        leftFormat: (String) -> String
    ): List<AccountLimitCardUiState> {
        val tabs = resolveTabs(profile)
        val resolvedTab = resolveSelectedTab(tabs, selectedTab)
        val sendTabTitle = tabs.firstOrNull { it.key == AccountLimitKey.SEND }?.title
        val receiveTabTitle = tabs.firstOrNull { it.key == AccountLimitKey.RECEIVE }?.title

        return profile.sectionTemplates.ifEmpty { AccountLimitCatalog.defaultSectionTemplates() }
            .map { template ->
                val limitAmount = valueProfile.limitFor(resolvedTab, template.key) ?: 0.0
                val formattedLimit = formatAmount(
                    currencySymbol = marketProfile.currencySymbol,
                    amount = limitAmount
                )
                val title = buildTitle(
                    rawTitle = template.titleTemplate,
                    formattedLimit = formattedLimit,
                    selectedTab = resolvedTab,
                    sendTabTitle = sendTabTitle,
                    receiveTabTitle = receiveTabTitle
                )

                val leadingLabel = when (template.type) {
                    AccountLimitSectionType.SINGLE -> formattedLimit
                    AccountLimitSectionType.PERIOD -> spentOfLimitFormat(
                        formatAmount(marketProfile.currencySymbol, 0.0),
                        formattedLimit
                    )
                }

                AccountLimitCardUiState(
                    key = template.key,
                    title = title,
                    progress = 0f,
                    leadingLabel = leadingLabel,
                    trailingLabel = when (template.type) {
                        AccountLimitSectionType.SINGLE -> formattedLimit
                        AccountLimitSectionType.PERIOD -> leftFormat(formattedLimit)
                    }
                )
            }
    }

    private fun buildTitle(
        rawTitle: String,
        formattedLimit: String,
        selectedTab: AccountLimitKey,
        sendTabTitle: String?,
        receiveTabTitle: String?
    ): String {
        val withAmount = rawTitle.replace("{amount}", formattedLimit)
        if (selectedTab != AccountLimitKey.RECEIVE) {
            return withAmount
        }

        if (!sendTabTitle.isNullOrBlank() && !receiveTabTitle.isNullOrBlank()) {
            val replaced = withAmount.replace(sendTabTitle, receiveTabTitle, ignoreCase = true)
            if (!replaced.equals(withAmount, ignoreCase = false)) {
                return replaced
            }
        }

        return SEND_WORD_REGEX.replace(withAmount, "Receive")
    }

    private fun formatAmount(currencySymbol: String, amount: Double): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        return currencySymbol + formatter.format(amount)
    }

    private val SEND_WORD_REGEX = Regex("\\bsend\\b", RegexOption.IGNORE_CASE)
}
