package net.metalbrain.paysmart.core.features.help.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileMenuItem
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpContactSupportScreen(
    onBack: () -> Unit,
    onTopicClick: (HelpSupportTopic) -> Unit
) {
    val topics = listOf(
        HelpSupportTopic.RECENT_TRANSFER,
        HelpSupportTopic.RECENT_FUNDING,
        HelpSupportTopic.ID_VERIFICATION,
        HelpSupportTopic.OTHER
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.help_contact_support_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            Text(
                text = stringResource(R.string.help_contact_support_heading),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    topics.forEachIndexed { index, topic ->
                        ProfileMenuItem(
                            title = stringResource(topic.categoryLabelRes),
                            onClick = { onTopicClick(topic) }
                        )
                        if (index < topics.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
