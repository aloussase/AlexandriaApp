package io.github.aloussase.booksdownloader.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aloussase.booksdownloader.domain.repository.SettingsRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val settings: SettingsRepository
) : ViewModel() {
    enum class Language {
        SPANISH,
        ENGLISH,
        GERMAN,
    }

    sealed class Event {
        data class OnLanguageSelected(val language: Language) : Event()
    }

    sealed class UiEvent {
        data class OnLanguageSelected(val language: Language) : UiEvent()
    }

    private val _uiEvents = MutableLiveData<UiEvent>()
    val uiEvents: LiveData<UiEvent> = _uiEvents

    init {
        viewModelScope.launch {
            val locale = settings.getSavedLocaled()
            val language = when (locale) {
                "es" -> Language.SPANISH
                "en" -> Language.ENGLISH
                "de" -> Language.GERMAN
                else -> return@launch
            }

            onLanguageSelected(language)
        }
    }

    fun onEvent(evt: Event) {
        when (evt) {
            is Event.OnLanguageSelected -> onLanguageSelected(evt.language)
        }
    }

    private fun onLanguageSelected(language: Language) {
        val lang = when (language) {
            Language.SPANISH -> "es"
            Language.ENGLISH -> "en"
            Language.GERMAN -> "de"
        }

        val locale = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(locale)

        viewModelScope.launch {
            settings.saveLocale(lang)

            _uiEvents.postValue(
                UiEvent.OnLanguageSelected(
                    language
                )
            )
        }
    }
}