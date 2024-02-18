package io.github.aloussase.booksdownloader.data

data class Book(
    val id: Long,
    val authors: List<String>,
    val title: String,
    val extension: String,
    val downloadUrl: String,
    val imageUrl: String,
)


fun Book.cover(): String {
    return "https://library.lol/$imageUrl"
}