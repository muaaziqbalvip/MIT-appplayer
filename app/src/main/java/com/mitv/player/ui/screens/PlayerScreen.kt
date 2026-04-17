package com.mitv.player.ui.screens

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mitv.player.domain.model.AspectRatio
import com.mitv.player.ui.viewmodel.MainViewModel
import com.mitv.player.ui.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    channelIndex: Int,
    onBack: () -> Unit,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val mainState by mainViewModel.uiState.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    val currentChannel by playerViewModel.currentChannel.collectAsState()
    val showControls by playerViewModel.showControls.collectAsState()

    // Load channel on first compose
    LaunchedEffect(channelIndex) {
        val channel = mainState.channels.getOrNull(channelIndex)
        channel?.let { playerViewModel.playChannel(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { playerViewModel.toggleControls() }
    ) {
        // ExoPlayer surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    player = playerViewModel.player
                }
            },
            update = { view ->
                view.resizeMode = when (playerState.aspectRatio) {
                    AspectRatio.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    AspectRatio.FILL -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    AspectRatio.SIXTEEN_NINE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    AspectRatio.FOUR_THREE -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                    AspectRatio.ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (playerState.isBuffering && !playerState.isError) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }

        // Error / Offline Fallback Screen
        if (playerState.isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A0E1A), Color(0xFF1A0A0A), Color(0xFF0A0E1A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = "https://i.ibb.co/5hPyzP10/1773218533375-removebg-preview.png",
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Icon(
                        Icons.Default.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFFFF6B6B)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Channel Busy / Offline",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        playerState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { playerViewModel.retry() },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onBack) {
                        Text("← Back to Channels", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // MiTV Watermark - bottom right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                AsyncImage(
                    model = "https://i.ibb.co/5hPyzP10/1773218533375-removebg-preview.png",
                    contentDescription = "MiTV",
                    modifier = Modifier.size(16.dp),
                    contentScale = ContentScale.Fit,
                    alpha = 0.65f
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "MiTV",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Player Controls Overlay
        AnimatedVisibility(
            visible = showControls && !playerState.isError,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            ) {
                // Top bar — back button + channel name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            currentChannel?.name ?: "Unknown Channel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            currentChannel?.groupTitle ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Center play/pause
                Box(modifier = Modifier.align(Alignment.Center)) {
                    IconButton(
                        onClick = { playerViewModel.togglePlayPause() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                        .align(Alignment.BottomStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Aspect ratio chip
                    Surface(
                        onClick = { playerViewModel.cycleAspectRatio() },
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            playerState.aspectRatio.label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    // LIVE indicator
                    Surface(
                        color = Color(0xFFFF4757),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
