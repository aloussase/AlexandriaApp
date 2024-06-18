package io.github.aloussase.booksdownloader.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.aloussase.booksdownloader.R
import io.github.aloussase.booksdownloader.behaviors.BottomNavigationViewBehavior
import io.github.aloussase.booksdownloader.databinding.ActivityMainBinding
import io.github.aloussase.booksdownloader.domain.repository.SettingsRepository
import io.github.aloussase.booksdownloader.receivers.DownloadManagerReceiver
import io.github.aloussase.booksdownloader.services.BookSearchService
import io.github.aloussase.booksdownloader.viewmodels.SnackbarViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val snackbarViewModel by viewModels<SnackbarViewModel>()

    @Inject
    lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setIcon(R.drawable.ic_toolbar_book)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHost.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navigation, navController)

        // Make the bottom navigation hide on scrolldown
        val layoutParams = binding.navigation.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.behavior = BottomNavigationViewBehavior()

        snackbarViewModel.isShowing.observe(this, ::showSnackbar)

        lifecycleScope.launch {
            DownloadManagerReceiver.isDownloadCompleted.collect {
                val message = getString(R.string.download_completed, it.bookTitle)
                snackbarViewModel.showSnackbar(message)
            }
        }

        lifecycleScope.launch {
            settings.setDefaults()
        }
    }

    private fun startSearchService(query: String) {
        val intent = Intent(this, BookSearchService::class.java).apply {
            putExtra("EXTRA_SEARCH_QUERY", query)
        }

        startService(intent)
    }

    private fun showSnackbar(show: Boolean) {
        if (show) {
            val snackbar = Snackbar.make(
                binding.root,
                snackbarViewModel.message.value!!,
                Snackbar.LENGTH_SHORT
            )

            snackbar.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    snackbarViewModel.hideSnackbar()
                }
            })

            snackbar.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SearchView

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        menuItem.collapseActionView()
                        startSearchService(query)
                    }

                    // Event was handled (do not show suggestions)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            }
        )

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return item.onNavDestinationSelected(findNavController(R.id.nav_host))
    }
}