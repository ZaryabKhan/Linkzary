package com.appcodecraft.linkzary.ui.screen.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _link = MutableStateFlow<SavedLink?>(null)
    val link: StateFlow<SavedLink?> = _link.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadLink(linkId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _link.value = linkRepository.getLinkById(linkId)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
