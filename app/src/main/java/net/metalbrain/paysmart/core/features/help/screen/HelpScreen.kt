package net.metalbrain.paysmart.core.features.help.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileAboutSocialsScreen
import net.metalbrain.paysmart.core.features.help.utils.buildSupportRequestBody
import net.metalbrain.paysmart.core.features.help.utils.openDialer
import net.metalbrain.paysmart.core.features.help.utils.openExternalUri
import net.metalbrain.paysmart.core.features.help.utils.openSupportRequestComposer
import net.metalbrain.paysmart.core.features.help.viewmodel.HelpViewModel
import net.metalbrain.paysmart.domain.model.Transaction

enum class HelpSupportTopic(
    @param:StringRes val categoryLabelRes: Int,
    @param:StringRes val listTitleRes: Int? = null
) {
    RECENT_TRANSFER(
        categoryLabelRes = R.string.help_topic_recent_transaction,
        listTitleRes = R.string.help_recent_transfer_title
    ),
    RECENT_FUNDING(
        categoryLabelRes = R.string.help_topic_recent_wallet_funding,
        listTitleRes = R.string.help_recent_funding_title
    ),
    ID_VERIFICATION(categoryLabelRes = R.string.help_topic_id_verification),
    OTHER(categoryLabelRes = R.string.help_topic_other)
}

private sealed interface HelpDestination {
    object Home : HelpDestination
    object ContactSupport : HelpDestination
    object Socials : HelpDestination
    data class RecentActivity(val topic: HelpSupportTopic) : HelpDestination
    data class Compose(
        val topic: HelpSupportTopic,
        val transaction: Transaction? = null
    ) : HelpDestination
}

@Composable
fun HelpScreen(
    navController: NavHostController,
    viewModel: HelpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val actionOpenFailedMessage = stringResource(R.string.profile_about_open_action_failed)
    val submitOpenFailedMessage = stringResource(R.string.help_submit_open_failed)
    val helpCenterUrl = stringResource(R.string.help_center_url)
    val supportEmail = stringResource(R.string.profile_about_contact_email)
    val supportPhoneNumber = stringResource(R.string.help_call_center_phone_number)
    val xUrl = stringResource(R.string.profile_about_social_x_url)
    val instagramUrl = stringResource(R.string.profile_about_social_instagram_url)
    val linkedInUrl = stringResource(R.string.profile_about_social_linkedin_url)
    val facebookUrl = stringResource(R.string.profile_about_social_facebook_url)

    var destinations by remember {
        mutableStateOf(listOf<HelpDestination>(HelpDestination.Home))
    }
    val currentDestination = destinations.last()

    fun push(destination: HelpDestination) {
        destinations = destinations + destination
    }

    fun pop() {
        if (destinations.size > 1) {
            destinations = destinations.dropLast(1)
        } else {
            navController.popBackStack()
        }
    }

    BackHandler(enabled = destinations.size > 1) {
        destinations = destinations.dropLast(1)
    }

    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    when (currentDestination) {
        HelpDestination.Home -> HelpHomeScreen(
            uiState = uiState,
            onBack = ::pop,
            onHelpCenterClick = {
                if (!openExternalUri(context, helpCenterUrl)) {
                    showToast(actionOpenFailedMessage)
                }
            },
            onContactSupportClick = { push(HelpDestination.ContactSupport) },
            onCallCenterClick = {
                if (!openDialer(context, supportPhoneNumber)) {
                    showToast(actionOpenFailedMessage)
                }
            },
            onSocialMediaClick = { push(HelpDestination.Socials) }
        )

        HelpDestination.ContactSupport -> HelpContactSupportScreen(
            onBack = ::pop,
            onTopicClick = { topic ->
                if (topic.listTitleRes != null) {
                    push(HelpDestination.RecentActivity(topic))
                } else {
                    push(HelpDestination.Compose(topic))
                }
            }
        )

        HelpDestination.Socials -> ProfileAboutSocialsScreen(
            onBack = ::pop,
            onXClick = {
                if (!openExternalUri(context, xUrl)) {
                    showToast(actionOpenFailedMessage)
                }
            },
            onInstagramClick = {
                if (!openExternalUri(context, instagramUrl)) {
                    showToast(actionOpenFailedMessage)
                }
            },
            onLinkedInClick = {
                if (!openExternalUri(context, linkedInUrl)) {
                    showToast(actionOpenFailedMessage)
                }
            },
            onFacebookClick = {
                if (!openExternalUri(context, facebookUrl)) {
                    showToast(actionOpenFailedMessage)
                }
            }
        )

        is HelpDestination.RecentActivity -> {
            val transactions = when (currentDestination.topic) {
                HelpSupportTopic.RECENT_TRANSFER -> uiState.recentTransfers
                HelpSupportTopic.RECENT_FUNDING -> uiState.recentFundings
                HelpSupportTopic.ID_VERIFICATION,
                HelpSupportTopic.OTHER -> emptyList()
            }

            HelpRecentActivityScreen(
                topic = currentDestination.topic,
                transactions = transactions,
                onBack = ::pop,
                onTransactionClick = { transaction ->
                    push(HelpDestination.Compose(currentDestination.topic, transaction))
                },
                onContinueWithoutSelection = {
                    push(HelpDestination.Compose(currentDestination.topic))
                }
            )
        }

        is HelpDestination.Compose -> {
            val topicLabel = stringResource(
                currentDestination.topic.listTitleRes ?: currentDestination.topic.categoryLabelRes
            )
            val emailSubject = stringResource(
                R.string.help_email_subject_format,
                topicLabel
            )

            HelpSupportComposeScreen(
                topic = currentDestination.topic,
                selectedTransaction = currentDestination.transaction,
                onBack = ::pop,
                onSubmit = { message, attachmentUri ->
                    val opened = openSupportRequestComposer(
                        context = context,
                        emailAddress = supportEmail,
                        subject = emailSubject,
                        body = buildSupportRequestBody(
                            topicLabel = topicLabel,
                            transaction = currentDestination.transaction,
                            message = message
                        ),
                        attachmentUri = attachmentUri
                    )

                    if (!opened) {
                        showToast(submitOpenFailedMessage)
                    }

                    opened
                }
            )
        }
    }
}
