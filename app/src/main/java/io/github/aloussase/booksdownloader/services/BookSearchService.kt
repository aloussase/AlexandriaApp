package io.github.aloussase.booksdownloader.services

import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.net.SocketTimeoutException
import java.net.URLEncoder

class BookSearchService : BaseApplicationService() {
    override val CHANNEL_ID = "BookSearchServiceChannel"
    override val NOTIFICATION_CHANNEL_NAME = "Book search notifications"
    val TAG = "BookSearchService"

    private var nextBookId: Long = 0

    private val acceptableBookFormats = listOf(
        "pdf",
        "epub",
        "azw3",
        "mobi"
    )

    sealed class State {
        data object Idle : State()
        data object Loading : State()
        data class GotResult(val books: List<Book>) : State()
        data object HadError : State()
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        val _state = MutableLiveData<State>(State.Idle)
        val state: LiveData<State> get() = _state

        val EXTRA_SEARCH_QUERY = "EXTRA_SEARCH_QUERY"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val searchQuery = intent?.getStringExtra(EXTRA_SEARCH_QUERY)

        if (searchQuery == null) {
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        createNotificationChannel()

        val notification = createNotification(
            getString(R.string.search_service_title, searchQuery),
            getString(R.string.search_service_content),
            createPendingIntent<MainActivity>(),
            R.drawable.ic_download_notification
        )

        startForeground(1, notification)

        scope.launch {
            try {
                startSearch(searchQuery)
            } catch (ex: Exception) {
                when (ex) {
                    is SocketTimeoutException,
                    is HttpStatusException -> {
                        withContext(Dispatchers.Main) {
                            _state.value = State.HadError
                            stopSelf()
                        }
                    }

                    else -> throw ex
                }

            }
        }

        return START_NOT_STICKY
    }

    private suspend fun startSearch(searchQuery: String) {
        _state.postValue(State.Loading)

        val searchUrl = createSearchUrl(searchQuery)
        val doc = Jsoup.connect(searchUrl).get()
        val rows = doc.select("tr:not(:first-child)")

        val data = mutableListOf<Book>()

        for (row in rows) {
            val book = parseRow(row) ?: continue
            data.add(book)
            _state.postValue(State.GotResult(data))
        }

        stopSelf()
    }

    private fun parseRow(row: Element): Book? {
        val authors = row.select("td:nth-child(2) > a")
            .textNodes()
            .map(TextNode::text)

        val title = row.selectFirst("td:nth-child(3) > a[title]")?.ownText() ?: return null

        val extension = row.select("td:nth-child(9)")
            .textNodes()
            .map(TextNode::text)
            .firstOrNull { it in acceptableBookFormats } ?: return null

        val size = row.select("td:nth-child(8)")
            .textNodes()
            .map(TextNode::text)
            .firstOrNull() ?: return null

        val mirrorsPageUrl = row
            .selectFirst("td:nth-child(10) > a[href]")?.attr("href")
            ?: return null

        val mirrorsDoc = Jsoup.connect(mirrorsPageUrl).get()

        // SSL Certificates by libgen seem to be self-signed or something
        // Another workaround would be to configure network to trust their certificates
        val downloadUrl = Uri.parse(
            mirrorsDoc
                .selectFirst("h2 > a[href]")
                ?.attr("href")
                ?.replace("https", "http")
        ) ?: return null

        val imageUrl = mirrorsDoc.selectFirst("img[src]")?.attr("src") ?: return null

        return Book(
            nextBookId++,
            authors,
            title,
            extension,
            downloadUrl,
            imageUrl,
            size
        )
    }

    private fun createSearchUrl(searchQuery: String): String {
        return buildString {
            append("https://libgen.is/search.php?req=")
            append(URLEncoder.encode(searchQuery, "UTF-8"))
            append("&res=50")
            append("&column=def")
            append("&sort=year")
            append("&sortmode=DESC")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}