package io.github.aloussase.booksdownloader.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Book(
    val id: Long,
    val authors: List<String>,
    val title: String,
    val extension: String,
    val downloadUrl: Uri,
    val imageUrl: String,
    val size: String,
) : Parcelable {
    companion object {}
}


fun Book.cover(): String {
    return "https://library.lol/$imageUrl"
}

fun Book.Companion.empty(): Book {
    return Book(
        id = 0,
        authors = emptyList(),
        title = "",
        extension = "",
        downloadUrl = Uri.parse("https://example.com"),
        imageUrl = "",
        size = ""
    )
}