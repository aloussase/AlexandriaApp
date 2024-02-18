package io.github.aloussase.booksdownloader.repositories

import io.github.aloussase.booksdownloader.data.Book

interface BookDownloadsRepository {
    suspend fun download(book: Book)
}