package com.griot.feature.editor.audio

import java.net.URI

/** The music services whose app opens their links; [OTHER] for any address. */
internal enum class LinkService(private val hosts: List<String>, val label: String?) {
    SPOTIFY(listOf("spotify.com", "spotify.link"), "Spotify"),
    YOUTUBE(listOf("youtube.com", "youtu.be"), "YouTube"),
    SOUNDCLOUD(listOf("soundcloud.com"), "SoundCloud"),
    DEEZER(listOf("deezer.com", "deezer.page.link"), "Deezer"),
    AUDIOMACK(listOf("audiomack.com"), "Audiomack"),
    APPLE_MUSIC(listOf("music.apple.com"), "Apple Music"),
    OTHER(emptyList(), null),
    ;

    companion object {
        /** The service of [url], from its host. */
        fun of(url: String): LinkService {
            val host = runCatching { URI(url).host }.getOrNull()?.lowercase() ?: return OTHER
            return entries.firstOrNull { service -> service.hosts.any { host == it || host.endsWith(".$it") } } ?: OTHER
        }
    }
}
