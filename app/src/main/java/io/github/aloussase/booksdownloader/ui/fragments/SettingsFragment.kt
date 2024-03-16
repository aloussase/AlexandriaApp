package io.github.aloussase.booksdownloader.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.databinding.FragmentSettingsBinding
import io.github.aloussase.booksdownloader.viewmodels.SettingsViewModel
import io.github.aloussase.booksdownloader.viewmodels.SettingsViewModel.Language
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding

    private val viewModel by viewModels<SettingsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupLanguageSettings()
        setupUiEvents()

        return binding.root
    }

    private fun setupUiEvents() {
        lifecycleScope.launch {
            viewModel.uiEvents.observe(viewLifecycleOwner) { evt ->
                when (evt) {
                    is SettingsViewModel.UiEvent.OnLanguageSelected -> {
                        binding.spinnerLanguage.setSelection(evt.language.ordinal)
                    }
                }
            }
        }
    }

    private fun setupLanguageSettings() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter

        val languageSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.onEvent(
                    SettingsViewModel.Event.OnLanguageSelected(
                        Language.entries[position]
                    )
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing.
            }

        }

        binding.spinnerLanguage.onItemSelectedListener = languageSelectedListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}