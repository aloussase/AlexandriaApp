package io.github.aloussase.booksdownloader.remote

data class AlexandriaPayload(
    val id: String,
    val path: String,
)

data class AlexandriaResult(
    val status: String,
    val data: AlexandriaPayload
)
