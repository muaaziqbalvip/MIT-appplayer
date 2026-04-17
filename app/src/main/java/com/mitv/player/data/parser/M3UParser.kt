package com.mitv.player.data.parser

import com.mitv.player.domain.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor() {

    suspend fun parseFromUrl(m3uUrl: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            val content = fetchContent(m3uUrl)
            parseContent(content)
        }
    }

    private fun fetchContent(urlStr: String): String {
        val url = URL(urlStr)
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "MiTV Player/1.0")
            instanceFollowRedirects = true
        }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    fun parseContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()

        if (!lines.firstOrNull()?.trim().orEmpty().startsWith("#EXTM3U")) {
            return emptyList()
        }

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val streamUrl = findNextStreamUrl(lines, i + 1)
                if (streamUrl != null) {
                    val channel = parseExtInf(line, streamUrl)
                    if (channel != null) channels.add(channel)
                    i = lines.indexOfFirst { it.trim() == streamUrl } + 1
                    continue
                }
            }
            i++
        }
        return channels
    }

    private fun findNextStreamUrl(lines: List<String>, fromIndex: Int): String? {
        for (i in fromIndex until minOf(fromIndex + 5, lines.size)) {
            val line = lines[i].trim()
            if (line.isNotEmpty() && !line.startsWith("#")) {
                return line
            }
        }
        return null
    }

    private fun parseExtInf(extInfLine: String, streamUrl: String): Channel? {
        return try {
            val name = extractChannelName(extInfLine)
            val logoUrl = extractAttribute(extInfLine, "tvg-logo") ?: ""
            val groupTitle = extractAttribute(extInfLine, "group-title") ?: "Uncategorized"
            val tvgId = extractAttribute(extInfLine, "tvg-id") ?: UUID.randomUUID().toString()

            Channel(
                id = tvgId.ifEmpty { UUID.randomUUID().toString() },
                name = name,
                streamUrl = streamUrl,
                logoUrl = logoUrl,
                groupTitle = groupTitle
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractChannelName(extInfLine: String): String {
        return extInfLine.substringAfterLast(",").trim().ifEmpty { "Unknown Channel" }
    }

    private fun extractAttribute(line: String, attribute: String): String? {
        val pattern = Regex("""$attribute="([^"]*?)"""")
        return pattern.find(line)?.groupValues?.getOrNull(1)?.trim()?.ifEmpty { null }
    }
}
