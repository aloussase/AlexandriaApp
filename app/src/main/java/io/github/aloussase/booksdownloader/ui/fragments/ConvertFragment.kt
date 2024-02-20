package io.github.aloussase.booksdownloader.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.data.BookFormat
import io.github.aloussase.booksdownloader.data.parse
import io.github.aloussase.booksdownloader.databinding.FragmentConvertBinding
import io.github.aloussase.booksdownloader.ui.MainActivity
import io.github.aloussase.booksdownloader.viewmodels.ConvertViewModel

@AndroidEntryPoint
class ConvertFragment : BaseApplicationFragment(R.layout.fragment_convert) {
    companion object {
        const val TAG = "ConvertFragment"
        const val PICK_FILE = 2
    }

    private lateinit var binding: FragmentConvertBinding

    private val convertViewModel: ConvertViewModel by activityViewModels()

    private var uploadedFileName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentConvertBinding.inflate(inflater, container, false)

        binding.btnChooseFile.setOnClickListener { openFile() }
        binding.btnConvert.setOnClickListener { convertBook() }

        binding.radioGroup.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbPdf -> selectConversionFormat(BookFormat.PDF)
                R.id.rbAzw3 -> selectConversionFormat(BookFormat.AZW3)
                R.id.rbEpub -> selectConversionFormat(BookFormat.EPUB)
                R.id.rbMobi -> selectConversionFormat(BookFormat.MOBI)
            }
        }

        convertViewModel.state.observe(viewLifecycleOwner, ::onConvertViewModelStateChanged)

        (activity as MainActivity).supportActionBar?.let {
            it.title = getString(R.string.convert_books_toolbar_title)
            it.setIcon(R.drawable.ic_toolbar_book)
        }

        convertViewModel.bookForConversion.observe(viewLifecycleOwner) { book ->
            // TODO: Download book from API
            Log.d(TAG, "Book: $book")
        }

        return binding.root
    }

    private fun onConvertViewModelStateChanged(newState: ConvertViewModel.State) {
        binding.btnConvert.isEnabled = newState.isFileUploaded

        val color = if (newState.isFileUploaded) R.color.green else R.color.gray
        binding.btnConvert.setTextColor(resources.getColor(color))

        setConversionFormat(newState.conversionFormat)

        newState.fileDisplayName?.let { filename ->
            if (filename != uploadedFileName) {
                uploadedFileName = filename
                snackBarViewModel.showSnackbar("Archivo cargado: $filename")
            }
        }
    }

    private fun convertBook() {
        val fromFormat = uploadedFileName?.split('.')?.lastOrNull()?.let {
            BookFormat.parse(it)
        }

        if (fromFormat == convertViewModel.state.value?.conversionFormat) {
            snackBarViewModel.showSnackbar("El archivo ya está en el formato seleccionado")
            return
        }

        convertViewModel.onEvent(ConvertViewModel.Event.OnConvertBook)
    }

    private fun setConversionFormat(format: BookFormat) {
        when (format) {
            BookFormat.PDF -> binding.rbPdf.isChecked = true
            BookFormat.AZW3 -> binding.rbAzw3.isChecked = true
            BookFormat.EPUB -> binding.rbEpub.isChecked = true
            BookFormat.MOBI -> binding.rbMobi.isChecked = true
        }
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