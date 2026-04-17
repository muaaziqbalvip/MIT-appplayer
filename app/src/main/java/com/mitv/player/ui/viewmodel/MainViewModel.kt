package com.mitv.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mitv.player.data.repository.ChannelRepository
import com.mitv.player.data.repository.PreferencesRepository
import com.mitv.player.domain.model.Channel
import com.mitv.player.domain.model.M3USource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val channels: List<Channel> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val m3uSources: List<M3USource> = emptyList(),
    val showOnboarding: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val channelRepo: ChannelRepository,
    private val prefsRepo: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())

    init {
        // Observe favorites
        viewModelScope.launch {
            prefsRepo.favorites.collect { favs ->
                favoriteIds.value = favs
                channelRepo.applyFavorites(favs)
                refreshDisplayedChannels()
            }
        }
        // Observe loading states
        viewModelScope.launch {
            channelRepo.isLoading.collect { loading ->
                _uiState.update { it.copy(isLoading = loading) }
            }
        }
        viewModelScope.launch {
            channelRepo.error.collect { err ->
                _uiState.update { it.copy(error = err) }
            }
        }
        // Observe sources from prefs
        viewModelScope.launch {
            prefsRepo.m3uSources.collect { sources ->
                _uiState.update { it.copy(m3uSources = sources) }
                if (sources.isEmpty()) {
                    _uiState.update { it.copy(showOnboarding = true) }
                }
            }
        }
        // Auto-load if URL saved
        viewModelScope.launch {
            prefsRepo.selectedM3UUrl.first().let { url ->
                if (url.isNotEmpty()) loadM3U(url)
            }
        }
    }

    fun loadM3U(url: String) = viewModelScope.launch {
        prefsRepo.setSelectedM3UUrl(url)
        channelRepo.loadChannels(url, favoriteIds.value)
        refreshDisplayedChannels()
    }

    fun addM3USource(name: String, url: String) = viewModelScope.launch {
        val current = prefsRepo.m3uSources.first().toMutableList()
        val source = M3USource(id = UUID.randomUUID().toString(), name = name, url = url)
        current.add(source)
        prefsRepo.saveM3USources(current)
        _uiState.update { it.copy(showOnboarding = false) }
        loadM3U(url)
    }

    fun removeM3USource(sourceId: String) = viewModelScope.launch {
        val current = prefsRepo.m3uSources.first().filter { it.id != sourceId }
        prefsRepo.saveM3USources(current)
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        refreshDisplayedChannels()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        refreshDisplayedChannels()
    }

    fun toggleFavorite(channelId: String) = viewModelScope.launch {
        prefsRepo.toggleFavorite(channelId)
    }

    private fun refreshDisplayedChannels() {
        val state = _uiState.value
        val query = state.searchQuery
        val category = state.selectedCategory

        val filtered = when {
            query.isNotBlank() -> channelRepo.searchChannels(query)
            else -> channelRepo.filterByCategory(category)
        }
        _uiState.update { it.copy(
            channels = filtered,
            categories = channelRepo.getCategories()
        )}
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
    fun dismissOnboarding() = _uiState.update { it.copy(showOnboarding = false) }
}
