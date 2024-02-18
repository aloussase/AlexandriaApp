package io.github.aloussase.booksdownloader.data

import java.util.Locale

fun BookFormat.name(): String {
    return when (this) {
        BookFormat.PDF -> "pdf"
        BookFormat.EPUB -> "epub"
        BookFormat.AZW3 -> "azw3"
        BookFormat.MOBI -> "mobi"
    }
}

fun BookFormat.Companion.parse(string: String): BookFormat {
    return when (string.lowercase(Locale.getDefault())) {
        "pdf" -> BookFormat.PDF
        "epub" -> BookFormat.EPUB
        "azw3" -> BookFormat.AZW3
        "mobi" -> BookFormat.MOBI
        else -> throw IllegalArgumentException("Invalid conversion format")
    }
}

enum class BookFormat {
    PDF,
    EPUB,
    AZW3,
    MOBI;

    companion object {}
}