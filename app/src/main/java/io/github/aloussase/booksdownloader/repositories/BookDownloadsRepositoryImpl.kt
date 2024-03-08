package io.github.aloussase.booksdownloader.repositories

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.domain.repository.BookDownloadsRepository
import javax.inject.Inject

class BookDownloadsRepositoryImpl @Inject constructor(
    val context: Context
) : BookDownloadsRepository {

    companion object {
        const val TAG = "BookDownloadsRepository"
    }

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override suspend fun download(book: Book, toUri: Uri) {
        val request = DownloadManager.Request(book.downloadUrl).apply {
            setTitle("Downloading ${book.title}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationUri(toUri)
        }

        Log.d(TAG, "Downloading book: ${book.title} to: $toUri")

        downloadManager.enqueue(request)
    }
}