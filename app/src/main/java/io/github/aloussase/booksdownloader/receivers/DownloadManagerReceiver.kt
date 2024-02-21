package io.github.aloussase.booksdownloader.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.aloussase.booksdownloader.data.DownloadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DownloadManagerReceiver : BroadcastReceiver() {
    val TAG = "DownloadManagerReceiver"

    val job = SupervisorJob()
    val scope = CoroutineScope(job + Dispatchers.Main)

    companion object {
        private val _isDownloadCompleted = Channel<DownloadResult>()
        val isDownloadCompleted = _isDownloadCompleted.receiveAsFlow()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
        if (id == -1L) {
            return
        }

        val query = DownloadManager.Query().apply {
            setFilterById(id)
            setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
        }
        val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = downloadManager.query(query)

        while (cursor.moveToNext()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(statusIndex)

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val uri = cursor.getString(uriIndex)
                val downloadedUri = Uri.parse(uri)

                val titleIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
                val title = cursor.getString(titleIndex)
                val bookTitle = title
                    .split(" ")
                    .drop(1)
                    .joinToString(" ")

                scope.launch {
                    _isDownloadCompleted.send(
                        DownloadResult(
                            bookTitle = bookTitle,
                            destinationUri = downloadedUri,
                        )
                    )
                }
            }
        }

        cursor.close()
    }
}