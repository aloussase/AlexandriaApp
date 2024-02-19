package io.github.aloussase.booksdownloader.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.parse
import io.github.aloussase.booksdownloader.repositories.BookConversionRepository
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConvertViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val conversions: BookConversionRepository,
    val downloads: BookDownloadsRepository
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

                Log.d(TAG, "Converting $title from $fromFormat to ${state.conversionFormat}")

                val result = conversions.convert(
                    fromFormat,
                    state.conversionFormat,
                    state.fileContents ?: return@launch
                )

                Log.d(TAG, "Conversion url: ${result.downloadUrl}")

                val book = Book(
                    title = title,
                    extension = extension,
                    downloadUrl = result.downloadUrl,
                    authors = emptyList(),
                    id = 0,
                    imageUrl = ""
                )

                downloads.download(book)
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

                Log.d(TAG, "Read ${bytes.size} bytes")

                _state.value = _state.value?.copy(
                    fileContents = bytes,
                    fileDisplayName = getUploadedFileName(uri),
                    isFileUploaded = true
                )
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
}