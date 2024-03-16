package io.github.aloussase.booksdownloader.domain.repository

interface SettingsRepository {
    /**
     * Get the saved localed
     */
    suspend fun getSavedLocaled(): String

    /**
     * Save the locale.
     */
    suspend fun saveLocale(language: String)

    /**
     * Prepare the default settings.
     */
    suspend fun setDefaults()
}