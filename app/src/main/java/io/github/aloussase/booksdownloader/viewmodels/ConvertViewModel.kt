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
import io.github.aloussase.booksdownloader.domain.use_case.ConvertBookUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val convertBook: ConvertBookUseCase,
) : ViewModel() {

    sealed class Event {
        data class OnSelectFromConversionFormat(val format: BookFormat) : Event()
        data class OnSelectToConversionFormat(val format: BookFormat) : Event()
        data class OnFileUploaded(val uri: Uri) : Event()
        data object OnConvertBook : Event()
    }

    sealed class Error(reason: String) {
        data class ConversionFailed(val reason: String) : Error(reason)
        data class LimitExceeded(val reason: String) : Error(reason)
        data class FileSizeExceeded(val reason: String) : Error(reason)
    }

    companion object {
        private const val TAG = "ConvertViewModel"
    }

    data class State(
        val fromConversionFormat: BookFormat,
        val toConversionFormat: BookFormat,
        val isFileUploaded: Boolean,
        val fileDisplayName: String?,
        val fileContents: ByteArray?,
    )

    private val contentResolver = context.contentResolver

    private val _state = MutableLiveData(
        State(
            BookFormat.PDF,
            BookFormat.PDF,
            false,
            null,
            null
        )
    )

    val state: LiveData<State> get() = _state

    private val _convertedBook = Channel<Book>()
    val convertedBook = _convertedBook.receiveAsFlow()

    private val _loadedFile = Channel<String>()
    val loadedFile = _loadedFile.receiveAsFlow()

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = Channel<Error>()
    val error = _error.receiveAsFlow()

    fun onEvent(evt: Event) {
        when (evt) {
            is Event.OnFileUploaded -> onFileUploaded(evt.uri)
            is Event.OnSelectFromConversionFormat -> onSelectFromConversionFormat(evt.format)
            is Event.OnSelectToConversionFormat -> onSelectToConversionFormat(evt.format)
            is Event.OnConvertBook -> onConvertBook()
        }
    }

    private fun onConvertBook() {
        _state.value?.let { state ->
            viewModelScope.launch {
                val filename = state.fileDisplayName ?: return@launch
                val title = filename.split('.').dropLast(1).joinToString()

                _isLoading.postValue(true)

                val result = convertBook.execute(
                    state.fromConversionFormat,
                    state.toConversionFormat,
                    filename,
                    state.fileContents ?: return@launch
                )

                _isLoading.postValue(false)

                when (result) {
                    is ConversionResult.Success -> {
                        val book = Book.empty().copy(
                            title = title,
                            extension = state.toConversionFormat.name.lowercase(),
                            downloadUrl = result.downloadUrl
                        )

                        _convertedBook.send(book)
                    }

                    is ConversionResult.EmptyContents -> {
                        _error.send(
                            Error.ConversionFailed(
                                context.getString(
                                    R.string.empty_contents
                                )
                            )
                        )
                    }

                    is ConversionResult.EmptyFilename -> {
                        _error.send(
                            Error.ConversionFailed(
                                context.getString(
                                    R.string.empty_filename
                                )
                            )
                        )
                    }

                    is ConversionResult.AlreadyInRightFormat -> {
                        _error.send(
                            Error.ConversionFailed(
                                context.getString(
                                    R.string.already_in_right_format
                                )
                            )
                        )
                    }

                    is ConversionResult.Error -> {
                        _error.send(
                            Error.ConversionFailed(
                                context.getString(
                                    R.string.there_was_an_error_converting_book
                                )
                            )
                        )
                    }

                    is ConversionResult.LimitReached -> {
                        _error.send(
                            Error.LimitExceeded(
                                context.getString(
                                    R.string.limit_reached
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onSelectFromConversionFormat(format: BookFormat) {
        _state.value = _state.value?.copy(
            fromConversionFormat = format
        )
    }

    private fun onSelectToConversionFormat(format: BookFormat) {
        _state.value = _state.value?.copy(
            toConversionFormat = format
        )
    }

    private fun onFileUploaded(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.use {
                val bytes = inputStream.readBytes()
                val filename = getUploadedFileName(uri)
                val fileSize = getUploadedFileSize(uri)

                if (fileSize != null && fileSize > 10) {
                    viewModelScope.launch {
                        _error.send(
                            Error.FileSizeExceeded(
                                context.getString(
                                    R.string.file_size_exceeded
                                )
                            )
                        )
                    }
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