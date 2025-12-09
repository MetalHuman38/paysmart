package net.metalbrain.paysmart.core.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun PasscodePrompt(onVerified: () -> Unit) {
    var code by remember { mutableStateOf("") }
    val viewModel: SecurityViewModel = hiltViewModel()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter your passcode")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Passcode") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            if (viewModel.verify(code)) {
                onVerified()
            }
        }) {
            Text("Unlock")
        }
    }
}
