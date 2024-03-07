package io.github.aloussase.booksdownloader.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.parse
import io.github.aloussase.booksdownloader.databinding.FragmentConvertBinding
import io.github.aloussase.booksdownloader.viewmodels.ConvertViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ConvertFragment : BaseApplicationFragment(R.layout.fragment_convert),
    AdapterView.OnItemSelectedListener {
    companion object {
        const val TAG = "ConvertFragment"
        const val PICK_FILE = 2
    }

    private lateinit var binding: FragmentConvertBinding

    private lateinit var arrayAdapter: ArrayAdapter<CharSequence>

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
        val spinnerFrom = binding.spinnerFromConversionFormat

        arrayAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.conversion_format_options,
            android.R.layout.simple_spinner_item
        )

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTo.adapter = arrayAdapter
        spinnerTo.onItemSelectedListener = this

        spinnerFrom.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.conversion_format_options,
            android.R.layout.simple_spinner_item
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

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
            convertViewModel.conversionError.collect { errorMsg ->
                snackBarViewModel.showSnackbar(errorMsg)
            }
        }

        lifecycleScope.launch {
            convertViewModel.loadedFile.collect { filename ->
                snackBarViewModel.showSnackbar(getString(R.string.file_loaded, filename))
            }
        }

        lifecycleScope.launch {
            convertViewModel.fileSizeExceeded.collect { fileSizeExceeded ->
                if (fileSizeExceeded) {
                    snackBarViewModel.showSnackbar(
                        getString(R.string.file_size_exceeded)
                    )
                }
            }
        }

        return binding.root
    }

    private fun onConvertViewModelStateChanged(newState: ConvertViewModel.State) {
        binding.btnConvert.isEnabled = newState.isFileUploaded

        setConversionFormat(newState.conversionFormat)

        newState.fileDisplayName?.let {
            binding.tvFileName.text = getString(R.string.archivo_cargado, it)
        }
    }

    private fun convertBook() {
        val fromFormat = convertViewModel.state.value
            ?.fileDisplayName
            ?.split('.')
            ?.lastOrNull()
            ?.let { BookFormat.parse(it) }

        if (fromFormat == convertViewModel.state.value?.conversionFormat) {
            snackBarViewModel.showSnackbar(getString(R.string.file_already_in_format))
            return
        }

        snackBarViewModel.showSnackbar(getString(R.string.starting_conversion))
        convertViewModel.onEvent(ConvertViewModel.Event.OnConvertBook)
    }

    private fun setConversionFormat(format: BookFormat) {
        binding.spinnerToConversionFormat.setSelection(
            format.ordinal
        )
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectConversionFormat(BookFormat.entries[position])
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        selectConversionFormat(BookFormat.entries[0])
    }

    private fun selectConversionFormat(format: BookFormat) {
        convertViewModel.onEvent(
            ConvertViewModel.Event.OnSelectConversionFormat(
                format
            )
        )
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