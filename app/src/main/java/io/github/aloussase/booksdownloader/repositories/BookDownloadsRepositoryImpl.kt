package io.github.aloussase.booksdownloader.repositories

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import io.github.aloussase.booksdownloader.data.Book
import javax.inject.Inject

class BookDownloadsRepositoryImpl @Inject constructor(
    val context: Context
) : BookDownloadsRepository {

    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override suspend fun download(book: Book) {
        // SSL Certificates by libgen seem to be self-signed or something
        // Another workaround would be to configure network to trust their certificates
        val uri = Uri.parse(book.downloadUrl.replace("https", "http"))

        val request = DownloadManager.Request(uri).apply {
            setTitle("Downloading ${book.title}")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${book.title}.${book.extension}"
            )
        }

        downloadManager.enqueue(request)
    }
}