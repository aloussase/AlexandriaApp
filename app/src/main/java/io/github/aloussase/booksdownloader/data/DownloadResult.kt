package io.github.aloussase.booksdownloader.data

import android.net.Uri

data class DownloadResult(
    val bookTitle: String,
    val destinationUri: Uri
)
