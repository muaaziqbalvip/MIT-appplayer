package com.mitv.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mitv.player.ui.components.ChannelCard
import com.mitv.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onChannelClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    val displayedChannels = if (showFavoritesOnly)
        uiState.channels.filter { it.isFavorite }
    else uiState.channels

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search channels...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://i.ibb.co/5hPyzP10/1773218533375-removebg-preview.png",
                                contentDescription = "MiTV",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("MiTV Player", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites",
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Categories row
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = uiState.selectedCategory == category,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category) }
                        )
                    }
                }
            }

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(16.dp))
                            Text("Loading channels...", color = MaterialTheme.colorScheme.onBackground.copy(0.6f))
                        }
                    }
                    uiState.error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.WifiOff, null, modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.height(16.dp))
                            Text("Failed to load", style = MaterialTheme.typography.titleMedium)
                            Text(uiState.error!!, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5f))
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { viewModel.dismissError() }) { Text("Retry") }
                        }
                    }
                    displayedChannels.isEmpty() && !uiState.isLoading -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Tv, null, modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(0.4f))
                            Spacer(Modifier.height(12.dp))
                            Text("No channels found", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 130.dp),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(displayedChannels) { index, channel ->
                                ChannelCard(
                                    channel = channel,
                                    onClick = { onChannelClick(index) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(channel.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Onboarding dialog
    if (uiState.showOnboarding) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissOnboarding() },
            title = { Text("Add M3U Playlist") },
            text = { Text("Go to Settings to add your M3U playlist URL and load channels.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissOnboarding() }) { Text("OK") }
            }
        )
    }
}
