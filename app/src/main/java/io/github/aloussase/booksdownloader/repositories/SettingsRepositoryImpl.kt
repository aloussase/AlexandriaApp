package io.github.aloussase.booksdownloader.repositories

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aloussase.booksdownloader.domain.repository.SettingsRepository
import java.util.Locale

class SettingsRepositoryImpl(
    @ApplicationContext val context: Context
) : SettingsRepository {
    override suspend fun getSavedLocaled(): String {
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("language", null)!!
    }

    override suspend fun saveLocale(language: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", language)
            .apply()
    }

    override suspend fun setDefaults() {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        var language = prefs.getString("language", null)
        if (language == null) {
            language = Locale.getDefault().language
            context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .edit()
                .putString("language", language)
                .apply()
        }

        val locale = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(locale)
    }
}