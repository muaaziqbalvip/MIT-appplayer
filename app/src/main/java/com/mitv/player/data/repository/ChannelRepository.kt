package com.mitv.player.data.repository

import com.mitv.player.data.parser.M3UParser
import com.mitv.player.domain.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepository @Inject constructor(
    private val parser: M3UParser,
    private val prefsRepo: PreferencesRepository
) {
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun loadChannels(m3uUrl: String, favoriteIds: Set<String> = emptySet()) {
        _isLoading.value = true
        _error.value = null
        parser.parseFromUrl(m3uUrl).fold(
            onSuccess = { list ->
                _channels.value = list.map { it.copy(isFavorite = favoriteIds.contains(it.id)) }
            },
            onFailure = { e ->
                _error.value = e.message ?: "Failed to load channels"
            }
        )
        _isLoading.value = false
    }

    fun applyFavorites(favoriteIds: Set<String>) {
        _channels.value = _channels.value.map { it.copy(isFavorite = favoriteIds.contains(it.id)) }
    }

    fun getCategories(): List<String> {
        return listOf("All") + _channels.value
            .map { it.groupTitle }
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()
    }

    fun filterByCategory(category: String): List<Channel> {
        return if (category == "All") _channels.value
        else _channels.value.filter { it.groupTitle == category }
    }

    fun searchChannels(query: String): List<Channel> {
        if (query.isBlank()) return _channels.value
        return _channels.value.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.groupTitle.contains(query, ignoreCase = true)
        }
    }

    fun getFavorites(): List<Channel> = _channels.value.filter { it.isFavorite }
}
