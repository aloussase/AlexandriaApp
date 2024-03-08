package io.github.aloussase.booksdownloader.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.domain.repository.BookDownloadsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDownloadsViewModel @Inject constructor(
    val downloads: BookDownloadsRepository
) : ViewModel() {

    sealed class Event {
        data class OnDownloadBook(val book: Book, val toUri: Uri) : Event()
    }

    fun onEvent(evt: Event) {
        when (evt) {
            is Event.OnDownloadBook -> onDownloadBook(evt.book, evt.toUri)
        }
    }

    private fun onDownloadBook(book: Book, toUri: Uri) {
        viewModelScope.launch {
            downloads.download(book, toUri)
        }
    }

}