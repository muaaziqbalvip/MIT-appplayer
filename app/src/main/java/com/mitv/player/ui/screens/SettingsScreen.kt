package com.mitv.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mitv.player.domain.model.ThemeMode
import com.mitv.player.ui.viewmodel.MainViewModel
import com.mitv.player.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by themeViewModel.themeMode.collectAsState()
    val uiState by mainViewModel.uiState.collectAsState()
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var newSourceName by remember { mutableStateOf("") }
    var newSourceUrl by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Theme Section
            item {
                SettingsSectionTitle("🎨 Theme")
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.values().forEach { mode ->
                            val label = when (mode) {
                                ThemeMode.DARK -> "🌙 Dark Mode"
                                ThemeMode.LIGHT -> "☀️ Light Mode"
                                ThemeMode.PREMIUM_GOLD -> "✨ Premium Gold"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = themeMode == mode,
                                    onClick = { themeViewModel.setTheme(mode) }
                                )
                                Text(
                                    label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (mode == ThemeMode.PREMIUM_GOLD) Color(0xFFFFD700)
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // M3U Sources Section
            item {
                SettingsSectionTitle("📡 M3U Playlists")
            }

            items(uiState.m3uSources) { source ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(source.name, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium)
                            Text(source.url, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.5f),
                                maxLines = 1)
                        }
                        IconButton(onClick = {
                            mainViewModel.loadM3U(source.url)
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { mainViewModel.removeM3USource(source.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { showAddSourceDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add M3U Playlist")
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showAddSourceDialog) {
        AlertDialog(
            onDismissRequest = { showAddSourceDialog = false },
            title = { Text("Add Playlist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newSourceName,
                        onValueChange = { newSourceName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSourceUrl,
                        onValueChange = { newSourceUrl = it },
                        label = { Text("M3U URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSourceName.isNotBlank() && newSourceUrl.isNotBlank()) {
                        mainViewModel.addM3USource(newSourceName, newSourceUrl)
                        newSourceName = ""; newSourceUrl = ""
                        showAddSourceDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSourceDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
