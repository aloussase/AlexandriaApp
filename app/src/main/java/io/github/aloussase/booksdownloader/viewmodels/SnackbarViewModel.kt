package io.github.aloussase.booksdownloader.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SnackbarViewModel : ViewModel() {

    private val _isShowing = MutableLiveData(false)
    val isShowing: LiveData<Boolean> get() = _isShowing

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun showSnackbar(message: String) {
        _message.value = message
        _isShowing.value = true
    }

    fun hideSnackbar() {
        _isShowing.value = false
        _message.value = ""
    }
}