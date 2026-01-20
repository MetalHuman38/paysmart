package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel

) {
    val security by viewModel.securitySettings.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { HomeBottomNavigation(
            navController = navController
        ) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                security == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    HomeContent(
                        security = security!!,
                        onProfileClick = {
                            navController.navigate(Screen.ProfileScreen.route)
                        },
                        onVerifyEmailClick = {
                            navController.navigate(Screen.AddEmail.route)
                        },
                        onAddAddressClick = {
                            navController.navigate(Screen.AddEmail.route)
                        },
                        onVerifyIdentityClick = {
                            navController.navigate(Screen.AddEmail.route)
                        }
                    )
                }
            }
        }
    }
}
