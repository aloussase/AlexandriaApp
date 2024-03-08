package io.github.aloussase.booksdownloader.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.databinding.FragmentConvertBinding
import io.github.aloussase.booksdownloader.viewmodels.ConvertViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConvertFragment : BaseApplicationFragment(R.layout.fragment_convert) {
    companion object {
        const val TAG = "ConvertFragment"
        const val PICK_FILE = 2
    }

    private val fromConversionFormatSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectFromConversionFormat(BookFormat.entries[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            selectFromConversionFormat(BookFormat.entries[0])
        }
    }

    private val toConversionFormatSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            selectToConversionFormat(BookFormat.entries[position])
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            selectToConversionFormat(BookFormat.entries[0])
        }
    }

    private lateinit var binding: FragmentConvertBinding
    private val convertViewModel: ConvertViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentConvertBinding.inflate(inflater, container, false)

        binding.btnChooseFile.setOnClickListener { openFile() }
        binding.btnConvert.setOnClickListener { convertBook() }

        val spinnerTo = binding.spinnerToConversionFormat
        val toConversionFormatAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.conversion_format_options,
            android.R.layout.simple_spinner_item
        )
        toConversionFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTo.adapter = toConversionFormatAdapter
        spinnerTo.onItemSelectedListener = toConversionFormatSelectedListener

        val spinnerFrom = binding.spinnerFromConversionFormat
        val fromConversionFormatAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.conversion_format_options,
            android.R.layout.simple_spinner_item
        )
        fromConversionFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = fromConversionFormatAdapter
        spinnerFrom.onItemSelectedListener = fromConversionFormatSelectedListener

        convertViewModel.state.observe(viewLifecycleOwner, ::onConvertViewModelStateChanged)

        convertViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.pbLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }

        lifecycleScope.launch {
            convertViewModel.convertedBook.collect { book ->
                snackBarViewModel.showSnackbar(getString(R.string.conversion_complete))
                setBookForDownload(book)
                downloadBook()
            }
        }

        lifecycleScope.launch {
            convertViewModel.error.collect { handleError(it) }
        }

        lifecycleScope.launch {
            convertViewModel.loadedFile.collect { filename ->
                snackBarViewModel.showSnackbar(getString(R.string.file_loaded, filename))
            }
        }

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    private fun onConvertViewModelStateChanged(newState: ConvertViewModel.State) {
        binding.btnConvert.isEnabled = newState.isFileUploaded

        binding.spinnerFromConversionFormat.setSelection(
            newState.fromConversionFormat.ordinal
        )

        binding.spinnerToConversionFormat.setSelection(
            newState.toConversionFormat.ordinal
        )

        newState.fileDisplayName?.let {
            binding.tvFileName.text = getString(R.string.archivo_cargado, it)
        }
    }

    private fun convertBook() {
        if (
            convertViewModel.state.value?.fromConversionFormat ==
            convertViewModel.state.value?.toConversionFormat
        ) {
            snackBarViewModel.showSnackbar(getString(R.string.file_already_in_format))
            return
        }

        snackBarViewModel.showSnackbar(getString(R.string.starting_conversion))
        convertViewModel.onEvent(ConvertViewModel.Event.OnConvertBook)
    }

    private fun selectFromConversionFormat(format: BookFormat) {
        convertViewModel.onEvent(
            ConvertViewModel.Event.OnSelectFromConversionFormat(
                format
            )
        )
    }

    private fun selectToConversionFormat(format: BookFormat) {
        convertViewModel.onEvent(
            ConvertViewModel.Event.OnSelectToConversionFormat(
                format
            )
        )
    }

    private fun handleError(error: ConvertViewModel.Error) {
        when (error) {
            is ConvertViewModel.Error.FileSizeExceeded ->
                snackBarViewModel.showSnackbar(error.reason)

            is ConvertViewModel.Error.LimitExceeded ->
                snackBarViewModel.showSnackbar(error.reason)

            is ConvertViewModel.Error.ConversionFailed ->
                snackBarViewModel.showSnackbar(error.reason)
        }
    }

    private fun openFile() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                val mimeTypes = arrayOf("application/pdf", "application/epub+zip")
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            },
            PICK_FILE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also {
                convertViewModel.onEvent(ConvertViewModel.Event.OnFileUploaded(it))
            }
        }
    }
}