package io.github.aloussase.booksdownloader.ui.fragments

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.viewmodels.BookDownloadsViewModel
import io.github.aloussase.booksdownloader.viewmodels.SnackbarViewModel
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

const val TAG = "BaseApplicationFragment"

@AndroidEntryPoint
open class BaseApplicationFragment(layoutId: Int) : Fragment(layoutId) {

    protected val snackBarViewModel by activityViewModels<SnackbarViewModel>()

    private val bookDownloadsViewModel by viewModels<BookDownloadsViewModel>()

    protected var bookToBeDownloaded: Book? = null

    companion object {
        const val RC_PERMS = 69
        const val BOOK_TO_BE_DOWNLOADED = "bookToBeDownloaded"
    }

    protected val readWritePerms = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    @RequiresApi(33)
    protected val notificationPerms = arrayOf(
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    protected fun setBookForDownload(book: Book) {
        bookToBeDownloaded = book
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(BOOK_TO_BE_DOWNLOADED, bookToBeDownloaded)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        bookToBeDownloaded = savedInstanceState?.getParcelable(BOOK_TO_BE_DOWNLOADED)
    }

    protected fun createEasyPermsOptions(perms: Array<String>): PermissionRequest {
        return PermissionRequest.Builder(
            this,
            RC_PERMS,
            *perms
        )
            .setRationale("Se necesita permiso para descargar el libro")
            .setPositiveButtonText("OK")
            .setNegativeButtonText("Cancelar")
            .build()
    }

    @AfterPermissionGranted(RC_PERMS)
    protected fun downloadBook() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !EasyPermissions.hasPermissions(requireContext(), *notificationPerms)
        ) {
            EasyPermissions.requestPermissions(
                createEasyPermsOptions(
                    notificationPerms
                )
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
            EasyPermissions.hasPermissions(requireContext(), *readWritePerms)
        ) {
            bookToBeDownloaded?.let { book ->
                val title = "${book.title}.${book.extension}"

                val uri: Uri = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toUri()
                    .buildUpon()
                    .appendPath(title)
                    .build()

                snackBarViewModel.showSnackbar(
                    getString(
                        R.string.starting_download,
                        book.title
                    )
                )

                bookDownloadsViewModel.onEvent(
                    BookDownloadsViewModel.Event.OnDownloadBook(
                        book,
                        uri
                    )
                )
            }
        } else {
            EasyPermissions.requestPermissions(
                createEasyPermsOptions(
                    readWritePerms
                )
            )
        }
    }

}