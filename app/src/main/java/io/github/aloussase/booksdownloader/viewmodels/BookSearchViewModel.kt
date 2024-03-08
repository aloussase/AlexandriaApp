package io.github.aloussase.booksdownloader.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.domain.use_case.FilterBooksUseCase
import javax.inject.Inject

@HiltViewModel
class BookSearchViewModel @Inject constructor(
    val filterBooks: FilterBooksUseCase
) : ViewModel() {

    sealed class Event {
        data class OnApplyFilter(val format: BookFormat) : Event()
        data class OnRemoveFilter(val format: BookFormat) : Event()
        data class OnBooksLoaded(val books: List<Book>) : Event()
    }

    private val _appliedFormatFilters = MutableLiveData<Set<BookFormat>>(
        setOf(
            BookFormat.PDF,
            BookFormat.EPUB,
            BookFormat.AZW3,
            BookFormat.MOBI
        )
    )
    val appliedFormatFilters: LiveData<Set<BookFormat>> get() = _appliedFormatFilters

    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> get() = _books

    private val _filteredBooks = MutableLiveData<List<Book>>()
    val filteredBooks: LiveData<List<Book>> get() = _filteredBooks

    fun onEvent(evt: Event) {
        when (evt) {
            is Event.OnApplyFilter -> onApplyFilter(evt.format)
            is Event.OnRemoveFilter -> onRemoveFilter(evt.format)
            is Event.OnBooksLoaded -> onBooksLoaded(evt.books)
        }
    }

    private fun onBooksLoaded(books: List<Book>) {
        _books.value = books
        updateFilteredBooks()
    }

    private fun onApplyFilter(format: BookFormat) {
        _appliedFormatFilters.value = _appliedFormatFilters.value?.plus(format)
        updateFilteredBooks()
    }

    private fun onRemoveFilter(format: BookFormat) {
        _appliedFormatFilters.value = _appliedFormatFilters.value?.minus(format)
        updateFilteredBooks()
    }

    private fun updateFilteredBooks() {
        _books.value?.let { books ->
            _appliedFormatFilters.value?.let { filters ->
                _filteredBooks.value = filterBooks.execute(books, filters)
            }
        }
    }

}