package net.metalbrain.paysmart.core.features.referral.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.core.features.referral.components.ReferralActionButtons
import net.metalbrain.paysmart.core.features.referral.components.ReferralFormSection
import net.metalbrain.paysmart.core.features.referral.components.ReferralHeroPlaceholder
import net.metalbrain.paysmart.core.features.referral.components.ReferralTopBar
import net.metalbrain.paysmart.core.features.referral.viewmodel.ReferralViewModel
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation

@Composable
fun ReferralScreen(
    navController: NavHostController,
    viewModel: ReferralViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { HomeBottomNavigation(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ReferralTopBar(
                onBack = {
                    navController.popBackStack()
                }
            )

            ReferralHeroPlaceholder()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 22.dp)
            ) {
                ReferralFormSection(
                    rewardLabel = uiState.rewardLabel,
                    thresholdLabel = uiState.transferThresholdLabel,
                    enteredCode = uiState.enteredCode,
                    onCodeChanged = viewModel::onReferralCodeChanged
                )

                Spacer(modifier = Modifier.weight(1f))

                ReferralActionButtons(
                    canSubmit = uiState.canSubmit,
                    isSubmitting = uiState.isSubmitting,
                    onSubmit = viewModel::submitReferralCode,
                    onNoCode = {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
