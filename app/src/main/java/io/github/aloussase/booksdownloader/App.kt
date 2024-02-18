package io.github.aloussase.booksdownloader

import android.app.DownloadManager
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import io.github.aloussase.booksdownloader.receivers.DownloadManagerReceiver

@HiltAndroidApp
class App : MultiDexApplication() {

    private val downloadReceiver = DownloadManagerReceiver()

    override fun onCreate() {
        super.onCreate()

        ContextCompat.registerReceiver(
            applicationContext,
            downloadReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }
}