package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileDetailRow
import net.metalbrain.paysmart.core.features.account.profile.util.ProfileInfoDivider
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ProfileInfoCard(
    user: AuthUserModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = Dimens.xs)) {
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_full_name),
                value = user.displayName
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_date_of_birth),
                value = user.dateOfBirth
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_address_line_1),
                value = user.addressLine1
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_address_line_2),
                value = user.addressLine2
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_city),
                value = user.city
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_email),
                value = user.email
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_phone_number),
                value = user.phoneNumber
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_country),
                value = user.country
            )
            ProfileInfoDivider()
            ProfileDetailRow(
                label = stringResource(R.string.profile_field_postal_code),
                value = user.postalCode
            )
        }
    }
}
