package net.metalbrain.paysmart


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import junit.framework.TestCase.assertEquals
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.di.AppModule
import net.metalbrain.paysmart.domain.model.Country
import net.metalbrain.paysmart.phone.PhoneVerifier
import net.metalbrain.paysmart.ui.viewmodel.CreateAccountViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, AppModule.PhoneModule::class)
class CreateAccountViewModelTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val instantExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject lateinit var authPolicyHandler: AuthPolicyHandler
    @Inject lateinit var phoneVerifier: PhoneVerifier

    lateinit var viewModel: CreateAccountViewModel

    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = CreateAccountViewModel(authPolicyHandler, phoneVerifier)
    }

    @Test
    fun getFullPhoneNumber_returnsE164Formatted() {
        viewModel.onPhoneNumberChanged("7988777954")
        viewModel.onCountrySelected(
            Country("gb", R.string.country_uk, R.drawable.flag_uk, "+44")
        )
        val full = viewModel.getFullPhoneNumber()
        assertEquals("+447988777954", full)
    }
}
