package io.github.aloussase.booksdownloader.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.adapters.BooksAdapter
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.databinding.FragmentHomeBinding
import io.github.aloussase.booksdownloader.receivers.DownloadManagerReceiver
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import io.github.aloussase.booksdownloader.services.BookSearchService
import io.github.aloussase.booksdownloader.ui.MainActivity
import io.github.aloussase.booksdownloader.viewmodels.SnackbarViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookSearchFragment : Fragment(R.layout.fragment_home) {
    val TAG = "BookSearchFragment"

    private val booksAdapter = BooksAdapter()

    private val snackBarViewModel by activityViewModels<SnackbarViewModel>()

    // TODO: Consider moving this to a ViewModel
    @Inject
    lateinit var bookDownloadsRepository: BookDownloadsRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        (activity as MainActivity).supportActionBar?.let {
            it.title = getString(R.string.search_books_toolbar_title)
            it.setIcon(R.drawable.ic_toolbar_book)
        }

        setupRecyclerView(binding)
        setupBookSearchObserver(binding)
        setupDownloadReceiver()

        booksAdapter.setOnItemDownloadListener(::startBookDownload)

        return binding.root
    }

    private fun setupDownloadReceiver() {
        DownloadManagerReceiver.notify.observe(viewLifecycleOwner) {
            snackBarViewModel.showSnackbar("Descarga completada: ${it.bookTitle}")
        }
    }

    private fun startBookDownload(book: Book) {
        snackBarViewModel.showSnackbar("Iniciando descarga de ${book.title}")
        lifecycleScope.launch {
            bookDownloadsRepository.download(book)
        }
    }

    private fun setupRecyclerView(binding: FragmentHomeBinding) {
        val rvBooks = binding.rvBooks
        rvBooks.apply {
            adapter = booksAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupBookSearchObserver(binding: FragmentHomeBinding) {
        BookSearchService.state.observe(viewLifecycleOwner) {
            when (it) {
                is BookSearchService.State.Idle -> {
                    binding.rvBooks.visibility = View.GONE
                    binding.llLoading.visibility = View.GONE
                    binding.tvGreeting.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }

                is BookSearchService.State.Loading -> {
                    binding.rvBooks.visibility = View.GONE
                    binding.llLoading.visibility = View.VISIBLE
                    binding.tvGreeting.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }

                is BookSearchService.State.GotResult -> {
                    booksAdapter.books = it.books

                    binding.rvBooks.visibility = View.VISIBLE
                    binding.llLoading.visibility = View.GONE
                    binding.tvGreeting.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }

                is BookSearchService.State.HadError -> {
                    binding.rvBooks.visibility = View.GONE
                    binding.tvGreeting.visibility = View.GONE
                    binding.llLoading.visibility = View.GONE
                    binding.tvError.visibility = View.VISIBLE
                }
            }
        }
    }
}