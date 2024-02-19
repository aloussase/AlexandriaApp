package io.github.aloussase.booksdownloader.data

import android.net.Uri

data class Book(
    val id: Long,
    val authors: List<String>,
    val title: String,
    val extension: String,
    val downloadUrl: Uri,
    val imageUrl: String,
)


fun Book.cover(): String {
    return "https://library.lol/$imageUrl"
}