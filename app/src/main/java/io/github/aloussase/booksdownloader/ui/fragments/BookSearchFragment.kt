package io.github.aloussase.booksdownloader.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.adapters.BooksAdapter
import io.github.aloussase.booksdownloader.data.Book
import io.github.aloussase.booksdownloader.databinding.FragmentHomeBinding
import io.github.aloussase.booksdownloader.receivers.DownloadManagerReceiver
import io.github.aloussase.booksdownloader.repositories.BookDownloadsRepository
import io.github.aloussase.booksdownloader.services.BookSearchService
import io.github.aloussase.booksdownloader.ui.MainActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookSearchFragment : Fragment(R.layout.fragment_home) {
    val TAG = "BookSearchFragment"

    private val booksAdapter = BooksAdapter()

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

        setupRecyclerView(binding)
        setupBookSearchObserver(binding)
        setupDownloadReceiver()

        booksAdapter.setOnItemDownloadListener(::startBookDownload)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        (activity as MainActivity).supportActionBar?.let {
            it.title = getString(R.string.app_name)
            it.setIcon(R.drawable.ic_toolbar_book)
        }
    }

    private fun setupDownloadReceiver() {
        DownloadManagerReceiver.notify.observe(viewLifecycleOwner) {
            showSnackbar("Descarga completada: ${it.bookTitle}")
        }
    }

    private fun startBookDownload(book: Book) {
        showSnackbar("Iniciando descarga de ${book.title}")
        lifecycleScope.launch {
            bookDownloadsRepository.download(book)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
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