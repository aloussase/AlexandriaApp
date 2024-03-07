package io.github.aloussase.booksdownloader.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.ConversionResult
import io.github.aloussase.booksdownloader.data.empty
import io.github.aloussase.booksdownloader.data.parse
import io.github.aloussase.booksdownloader.repositories.BookConversionRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val conversions: BookConversionRepository,
) : ViewModel() {

    sealed class Event {
        data class OnSelectConversionFormat(val format: BookFormat) : Event()
        data class OnFileUploaded(val uri: Uri) : Event()
        data object OnConvertBook : Event()
    }

    companion object {
        private const val TAG = "ConvertViewModel"
    }

    data class State(
        val conversionFormat: BookFormat,
        val isFileUploaded: Boolean,
        val fileDisplayName: String?,
        val fileContents: ByteArray?,
    )

    private val contentResolver = context.contentResolver

    private val _state = MutableLiveData(
        State(
            BookFormat.PDF,
            false,
            null,
            null
        )
    )

    val state: LiveData<State> get() = _state

    private val _convertedBook = Channel<Book>()
    val convertedBook = _convertedBook.receiveAsFlow()

    private val _conversionError = Channel<String>()
    val conversionError = _conversionError.receiveAsFlow()

    private val _loadedFile = Channel<String>()
    val loadedFile = _loadedFile.receiveAsFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _fileSizeExceeded = Channel<Boolean>()
    val fileSizeExceeded = _fileSizeExceeded.receiveAsFlow()

    fun onEvent(evt: Event) {
        when (evt) {
            is Event.OnFileUploaded -> onFileUploaded(evt.uri)
            is Event.OnSelectConversionFormat -> onSelectConversionFormat(evt.format)
            is Event.OnConvertBook -> onConvertBook()
        }
    }

    private fun onConvertBook() {
        val state = _state.value
        if (state != null) {
            viewModelScope.launch {
                val filename = state.fileDisplayName ?: return@launch
                val title = filename.split('.').dropLast(1).joinToString()
                val extension = filename.split('.').last()
                val fromFormat = BookFormat.parse(extension)

                _isLoading.postValue(true)

                val result = conversions.convert(
                    fromFormat,
                    state.conversionFormat,
                    filename,
                    state.fileContents ?: return@launch
                )

                _isLoading.postValue(false)

                when (result) {
                    is ConversionResult.Success -> {
                        val book = Book.empty().copy(
                            title = title,
                            extension = state.conversionFormat.name.lowercase(),
                            downloadUrl = result.downloadUrl
                        )

                        _convertedBook.send(book)
                    }

                    is ConversionResult.Error -> {
                        _conversionError.send(
                            context.getString(
                                R.string.there_was_an_error_converting_book
                            )
                        )
                    }

                    is ConversionResult.LimitReached -> {
                        _conversionError.send(
                            context.getString(
                                R.string.limit_reached
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onSelectConversionFormat(format: BookFormat) {
        _state.value = _state.value?.copy(
            conversionFormat = format
        )
    }

    private fun onFileUploaded(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.use {
                val bytes = inputStream.readBytes()
                val filename = getUploadedFileName(uri)
                val fileSize = getUploadedFileSize(uri)

                if (fileSize != null && fileSize > 10) {
                    viewModelScope.launch { _fileSizeExceeded.send(true) }
                    return
                }

                if (filename != null) {
                    viewModelScope.launch {
                        _loadedFile.send(filename)
                    }

                    _state.value = _state.value?.copy(
                        fileContents = bytes,
                        fileDisplayName = filename,
                        isFileUploaded = true
                    )
                }
            }
        }
    }

    private fun getUploadedFileName(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val displayName = it.getString(displayNameIndex)
                return displayName
            }
        }

        return null
    }

    private fun getUploadedFileSize(uri: Uri): Int? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val fileSizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                val fileSize = it.getInt(fileSizeIndex)
                return fileSize / (1024 * 1024)
            }
        }

        return null
    }
}