package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Country

@Composable
fun PhoneNumberInput(
    selectedCountry: Country,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onFlagClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp)
    ) {
        // ⬇️ Country selector with box and dropdown icon
        Box(
            modifier = Modifier
                .clickable { onFlagClick() }
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = selectedCountry.flagRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Country",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 📞 Dial code
        Text(
            text = selectedCountry.dialCode,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(min = 40.dp),
            fontSize = 16.sp
        )

        // 📝 Phone number text field
        TextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            placeholder = { Text(text = stringResource(R.string.phone_place_holder)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
