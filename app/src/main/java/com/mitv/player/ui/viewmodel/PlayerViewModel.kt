package com.mitv.player.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.mitv.player.domain.model.AspectRatio
import com.mitv.player.domain.model.Channel
import com.mitv.player.domain.model.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()

    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()

    private var hideControlsJob: Job? = null
    private var positionTrackJob: Job? = null

    // Optimized LoadControl for low-latency live streams
    private val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(
            /* minBufferMs */ 15_000,
            /* maxBufferMs */ 50_000,
            /* bufferForPlaybackMs */ 2_000,
            /* bufferForPlaybackAfterRebufferMs */ 5_000
        )
        .build()

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setLoadControl(loadControl)
        .build()
        .also { exo ->
            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    _playerState.update {
                        it.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            isPlaying = exo.isPlaying,
                            isError = false
                        )
                    }
                    if (playbackState == Player.STATE_READY) startPositionTracking()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.update { it.copy(isPlaying = isPlaying) }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _playerState.update {
                        it.copy(
                            isError = true,
                            isBuffering = false,
                            isPlaying = false,
                            errorMessage = error.message ?: "Stream unavailable"
                        )
                    }
                }
            })
        }

    fun playChannel(channel: Channel) {
        _currentChannel.value = channel
        _playerState.update { PlayerState(isBuffering = true) }
        val mediaItem = MediaItem.fromUri(channel.streamUrl)
        player.apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
        showControlsTemporarily()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        showControlsTemporarily()
    }

    fun retry() {
        _currentChannel.value?.let { playChannel(it) }
    }

    fun cycleAspectRatio() {
        val ratios = AspectRatio.values()
        val current = _playerState.value.aspectRatio
        val next = ratios[(current.ordinal + 1) % ratios.size]
        _playerState.update { it.copy(aspectRatio = next) }
        showControlsTemporarily()
    }

    fun toggleControls() {
        if (_showControls.value) {
            _showControls.value = false
            hideControlsJob?.cancel()
        } else {
            showControlsTemporarily()
        }
    }

    fun showControlsTemporarily() {
        _showControls.value = true
        hideControlsJob?.cancel()
        hideControlsJob = viewModelScope.launch {
            delay(4000)
            _showControls.value = false
        }
    }

    private fun startPositionTracking() {
        positionTrackJob?.cancel()
        positionTrackJob = viewModelScope.launch {
            while (isActive && player.isPlaying) {
                _playerState.update {
                    it.copy(
                        position = player.currentPosition,
                        duration = player.duration
                    )
                }
                delay(500)
            }
        }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
