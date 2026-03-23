package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.ProfileAboutActionItem

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
