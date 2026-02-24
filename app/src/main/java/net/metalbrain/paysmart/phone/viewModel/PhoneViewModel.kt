package net.metalbrain.paysmart.phone.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.phone.data.PhoneDraft
import net.metalbrain.paysmart.phone.data.PhoneDraftStore

@HiltViewModel
class PhoneViewModel @Inject constructor(
    private val store: PhoneDraftStore
) : ViewModel() {

    val phoneDraft = store.draft.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(),
        PhoneDraft()
    )

    fun saveDraft(draft: PhoneDraft) {
        viewModelScope.launch {
            store.saveDraft(draft)
        }
    }
}
