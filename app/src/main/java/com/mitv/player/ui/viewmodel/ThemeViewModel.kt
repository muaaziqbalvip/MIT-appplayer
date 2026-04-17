package com.mitv.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitv.player.data.repository.PreferencesRepository
import com.mitv.player.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    val themeMode = prefsRepo.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.DARK
    )

    fun setTheme(mode: ThemeMode) = viewModelScope.launch {
        prefsRepo.setThemeMode(mode)
    }
}
