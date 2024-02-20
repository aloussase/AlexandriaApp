package io.github.aloussase.booksdownloader.repositories

import android.net.Uri
import io.github.aloussase.booksdownloader.data.Book

interface BookDownloadsRepository {
    suspend fun download(book: Book, toUri: Uri)
}