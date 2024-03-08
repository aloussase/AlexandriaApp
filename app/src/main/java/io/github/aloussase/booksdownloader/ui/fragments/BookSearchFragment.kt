package io.github.aloussase.booksdownloader.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.adapters.BooksAdapter
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.databinding.FragmentHomeBinding
import io.github.aloussase.booksdownloader.services.BookSearchService
import io.github.aloussase.booksdownloader.viewmodels.BookSearchViewModel

@AndroidEntryPoint
class BookSearchFragment : BaseApplicationFragment(R.layout.fragment_home) {
    val TAG = "BookSearchFragment"

    private val booksAdapter = BooksAdapter()

    private val bookSearchViewModel by viewModels<BookSearchViewModel>()

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        setupRecyclerView()
        setupBookSearchObserver()
        setupFormatFilters()

        booksAdapter.setOnItemDownloadListener { book ->
            setBookForDownload(book)
            downloadBook()
        }

        bookSearchViewModel.filteredBooks.observe(viewLifecycleOwner) {
            booksAdapter.books = it
        }

        bookSearchViewModel.appliedFormatFilters.observe(viewLifecycleOwner) {
            binding.filterPdf.isChecked = it.contains(BookFormat.PDF)
            binding.filterEpub.isChecked = it.contains(BookFormat.EPUB)
            binding.filterAzw3.isChecked = it.contains(BookFormat.AZW3)
            binding.filterMobi.isChecked = it.contains(BookFormat.MOBI)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        askForNotificationPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.action_filter)?.isVisible = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val booksEmpty = bookSearchViewModel.books.value?.isEmpty() ?: return
        menu.findItem(R.id.action_filter)?.isVisible = !booksEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                binding.filters.isVisible = !binding.filters.isVisible
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupFormatFilters() {
        binding.filterPdf.setOnClickListener(createFilterClickListener(BookFormat.PDF))
        binding.filterEpub.setOnClickListener(createFilterClickListener(BookFormat.EPUB))
        binding.filterAzw3.setOnClickListener(createFilterClickListener(BookFormat.AZW3))
        binding.filterMobi.setOnClickListener(createFilterClickListener(BookFormat.MOBI))
    }

    private fun createFilterClickListener(format: BookFormat) = { view: View ->
        if ((view as CheckBox).isChecked) {
            bookSearchViewModel.onEvent(BookSearchViewModel.Event.OnApplyFilter(format))
        } else {
            bookSearchViewModel.onEvent(BookSearchViewModel.Event.OnRemoveFilter(format))
        }
    }

    private fun setupRecyclerView() {
        binding.rvBooks.apply {
            adapter = booksAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupBookSearchObserver() {
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
                    bookSearchViewModel.onEvent(BookSearchViewModel.Event.OnBooksLoaded(it.books))
                    requireActivity().invalidateOptionsMenu()

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