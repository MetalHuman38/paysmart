package net.metalbrain.paysmart.core.features.account.profile.screen


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.core.features.account.profile.data.ProfileAboutActionItem


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
