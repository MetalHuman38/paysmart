package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileMenuItem
import net.metalbrain.paysmart.ui.theme.Dimens

private data class ProfileAboutActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileAboutScreen(
    onBack: () -> Unit,
    onLegalClick: () -> Unit,
    onSocialMediaClick: () -> Unit,
    onBlogClick: () -> Unit,
    onAppRatingClick: () -> Unit,
    onContactUsClick: () -> Unit
) {
    val items = listOf(
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_legal_title),
            icon = Icons.Default.Description,
            onClick = onLegalClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_social_media_title),
            icon = Icons.Default.Public,
            onClick = onSocialMediaClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_blog_title),
            icon = Icons.Default.Language,
            onClick = onBlogClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_app_rating_title),
            icon = Icons.Default.StarOutline,
            onClick = onAppRatingClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_contact_us_title),
            icon = Icons.Default.AlternateEmail,
            onClick = onContactUsClick
        )
    )

    ProfileAboutActionListScreen(
        title = stringResource(R.string.profile_menu_about_title),
        items = items,
        onBack = onBack
    )
}

@Composable
fun ProfileAboutSocialsScreen(
    onBack: () -> Unit,
    onXClick: () -> Unit,
    onInstagramClick: () -> Unit,
    onLinkedInClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    val items = listOf(
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_social_x_title),
            icon = Icons.Default.Public,
            onClick = onXClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_social_instagram_title),
            icon = Icons.Default.PhotoCamera,
            onClick = onInstagramClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_social_linkedin_title),
            icon = Icons.Default.Business,
            onClick = onLinkedInClick
        ),
        ProfileAboutActionItem(
            title = stringResource(R.string.profile_about_social_facebook_title),
            icon = Icons.Default.Groups,
            onClick = onFacebookClick
        )
    )

    ProfileAboutActionListScreen(
        title = stringResource(R.string.profile_about_socials_title),
        items = items,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileAboutActionListScreen(
    title: String,
    items: List<ProfileAboutActionItem>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { androidx.compose.material3.Text(text = title) },
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
                .padding(
                    horizontal = Dimens.mediumScreenPadding,
                    vertical = Dimens.md
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    items.forEachIndexed { index, item ->
                        ProfileMenuItem(
                            title = item.title,
                            leadingIcon = item.icon,
                            leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = item.onClick
                        )
                        if (index < items.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
