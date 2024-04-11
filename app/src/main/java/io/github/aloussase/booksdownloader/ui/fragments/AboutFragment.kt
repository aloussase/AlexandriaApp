package io.github.aloussase.booksdownloader.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.databinding.FragmentAboutBinding

class AboutFragment : BaseApplicationFragment(R.layout.fragment_about) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val binding = FragmentAboutBinding.inflate(inflater, container, false)

        binding.ibGithub.setOnClickListener { goToUrl("https://github.com/aloussase/AlexandriaApp") }
        binding.ibLinkedin.setOnClickListener { goToUrl("https://www.linkedin.com/in/alexander-goussas/") }
        binding.ibKofi.setOnClickListener { goToUrl("https://ko-fi.com/aloussase") }

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

    private fun goToUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}