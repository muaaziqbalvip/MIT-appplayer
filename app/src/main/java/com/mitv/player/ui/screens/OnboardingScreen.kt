package com.mitv.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mitv.player.ui.viewmodel.MainViewModel

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var m3uUrl by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("My Playlist") }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            AsyncImage(
                model = "https://i.ibb.co/5hPyzP10/1773218533375-removebg-preview.png",
                contentDescription = "MiTV Logo",
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "MiTV Player",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Enter your M3U playlist to get started",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Source Name
            OutlinedTextField(
                value = sourceName,
                onValueChange = { sourceName = it },
                label = { Text("Playlist Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // M3U URL input
            OutlinedTextField(
                value = m3uUrl,
                onValueChange = { m3uUrl = it },
                label = { Text("M3U URL") },
                placeholder = { Text("http://yourprovider.com/playlist.m3u") },
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                }),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    keyboard?.hide()
                    if (m3uUrl.isNotBlank()) {
                        viewModel.addM3USource(sourceName, m3uUrl.trim())
                        onDone()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = m3uUrl.isNotBlank()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Load Playlist", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (uiState.error != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
