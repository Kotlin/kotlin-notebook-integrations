package org.jetbrains.kotlinx.jupyter.widget.library.media

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.library.AudioWidget
import org.jetbrains.kotlinx.jupyter.widget.library.ImageWidget
import org.jetbrains.kotlinx.jupyter.widget.library.VideoWidget
import org.jetbrains.kotlinx.jupyter.widget.library.audio
import org.jetbrains.kotlinx.jupyter.widget.library.image
import org.jetbrains.kotlinx.jupyter.widget.library.video

/**
 * The URL of the media content.
 * When set, the [MediaWidget.value] is updated with the URL bytes and [MediaWidget.format] is set to "url".
 * When read, attempts to decode the URL from [MediaWidget.value] if [MediaWidget.format] is "url".
 */
public var MediaWidget.url: String?
    get() = if (format == "url") value.decodeToString() else null
    set(v) {
        if (v != null) {
            value = v.encodeToByteArray()
            format = "url"
        }
    }

/**
 * Creates an [org.jetbrains.kotlinx.jupyter.widget.library.AudioWidget] and sets its value from a URL.
 *
 * @param url The URL of the audio.
 * @param setup Additional setup for the widget.
 * @return A new [org.jetbrains.kotlinx.jupyter.widget.library.AudioWidget].
 */
public fun WidgetManager.audio(
    url: String,
    setup: AudioWidget.() -> Unit = {},
): AudioWidget =
    audio {
        this.url = url
        setup()
    }

/**
 * Creates an [org.jetbrains.kotlinx.jupyter.widget.library.ImageWidget] and sets its value from a URL.
 *
 * @param url The URL of the image.
 * @param setup Additional setup for the widget.
 * @return A new [org.jetbrains.kotlinx.jupyter.widget.library.ImageWidget].
 */
public fun WidgetManager.image(
    url: String,
    setup: ImageWidget.() -> Unit = {},
): ImageWidget =
    image {
        this.url = url
        setup()
    }

/**
 * Creates a [org.jetbrains.kotlinx.jupyter.widget.library.VideoWidget] and sets its value from a URL.
 *
 * @param url The URL of the video.
 * @param setup Additional setup for the widget.
 * @return A new [org.jetbrains.kotlinx.jupyter.widget.library.VideoWidget].
 */
public fun WidgetManager.video(
    url: String,
    setup: VideoWidget.() -> Unit = {},
): VideoWidget =
    video {
        this.url = url
        setup()
    }
