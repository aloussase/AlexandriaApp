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
) : Parcelable


fun Book.cover(): String {
    return "https://library.lol/$imageUrl"
}