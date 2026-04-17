package com.mitv.player.domain.model

data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val groupTitle: String = "Uncategorized",
    val isFavorite: Boolean = false
)

data class M3USource(
    val id: String,
    val name: String,
    val url: String,
    val lastUpdated: Long = 0L
)

enum class ThemeMode {
    DARK, LIGHT, PREMIUM_GOLD
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val aspectRatio: AspectRatio = AspectRatio.FIT,
    val duration: Long = 0L,
    val position: Long = 0L
)

enum class AspectRatio(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    SIXTEEN_NINE("16:9"),
    FOUR_THREE("4:3"),
    ZOOM("Zoom")
}
