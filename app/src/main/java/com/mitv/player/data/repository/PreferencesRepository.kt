package com.mitv.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mitv.player.domain.model.Channel
import com.mitv.player.domain.model.M3USource
import com.mitv.player.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mitv_prefs")

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val M3U_SOURCES_KEY = stringPreferencesKey("m3u_sources")
        val FAVORITES_KEY = stringPreferencesKey("favorites")
        val SELECTED_M3U_URL_KEY = stringPreferencesKey("selected_m3u_url")
    }

    // Theme
    val themeMode: Flow<ThemeMode> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }.map { prefs ->
        ThemeMode.valueOf(prefs[THEME_MODE_KEY] ?: ThemeMode.DARK.name)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }

    // M3U Sources
    val m3uSources: Flow<List<M3USource>> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }.map { prefs ->
        val json = prefs[M3U_SOURCES_KEY] ?: return@map emptyList()
        val type = object : TypeToken<List<M3USource>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    suspend fun saveM3USources(sources: List<M3USource>) {
        dataStore.edit { it[M3U_SOURCES_KEY] = gson.toJson(sources) }
    }

    // Favorites
    val favorites: Flow<Set<String>> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }.map { prefs ->
        val json = prefs[FAVORITES_KEY] ?: return@map emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        gson.fromJson(json, type) ?: emptySet()
    }

    suspend fun toggleFavorite(channelId: String) {
        dataStore.edit { prefs ->
            val json = prefs[FAVORITES_KEY]
            val type = object : TypeToken<MutableSet<String>>() {}.type
            val favs: MutableSet<String> = if (json != null) gson.fromJson(json, type) else mutableSetOf()
            if (favs.contains(channelId)) favs.remove(channelId) else favs.add(channelId)
            prefs[FAVORITES_KEY] = gson.toJson(favs)
        }
    }

    suspend fun clearFavorites() {
        dataStore.edit { it.remove(FAVORITES_KEY) }
    }

    // Selected M3U URL
    val selectedM3UUrl: Flow<String> = dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }.map { it[SELECTED_M3U_URL_KEY] ?: "" }

    suspend fun setSelectedM3UUrl(url: String) {
        dataStore.edit { it[SELECTED_M3U_URL_KEY] = url }
    }

    // Backup / Export
    fun exportSettings(channels: List<Channel>, sources: List<M3USource>): String {
        val backup = mapOf(
            "sources" to sources,
            "favorites" to channels.filter { it.isFavorite }.map { it.id }
        )
        return gson.toJson(backup)
    }
}
