package io.github.aloussase.booksdownloader.data

import android.net.Uri


sealed class ConversionResult {
    data class Success(val downloadUrl: Uri) : ConversionResult()
    data object Error : ConversionResult()
    data object LimitReached : ConversionResult()
}