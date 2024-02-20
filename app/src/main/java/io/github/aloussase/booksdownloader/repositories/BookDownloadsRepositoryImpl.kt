package io.github.aloussase.booksdownloader.repositories

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import io.github.aloussase.booksdownloader.data.Book
import javax.inject.Inject

class BookDownloadsRepositoryImpl @Inject constructor(
    val context: Context
) : BookDownloadsRepository {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override suspend fun download(book: Book, toUri: Uri) {
        val request = DownloadManager.Request(book.downloadUrl).apply {
            setTitle("Downloading ${book.title}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(toUri)
        }

        downloadManager.enqueue(request)
    }
}