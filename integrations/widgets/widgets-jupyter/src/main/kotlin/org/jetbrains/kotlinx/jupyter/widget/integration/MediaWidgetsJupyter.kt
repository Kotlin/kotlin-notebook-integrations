package org.jetbrains.kotlinx.jupyter.widget.integration

import org.jetbrains.kotlinx.jupyter.widget.library.AudioWidget
import org.jetbrains.kotlinx.jupyter.widget.library.ImageWidget
import org.jetbrains.kotlinx.jupyter.widget.library.VideoWidget
import org.jetbrains.kotlinx.jupyter.widget.library.audio
import org.jetbrains.kotlinx.jupyter.widget.library.image
import org.jetbrains.kotlinx.jupyter.widget.library.video

/**
 * Creates an [AudioWidget] from a URL and registers it with the global widget manager.
 *
 * @param url The URL of the audio.
 * @param setup A lambda to configure the widget after creation.
 * @return The created and registered [AudioWidget].
 */
public fun audioWidget(
    url: String,
    setup: AudioWidget.() -> Unit = {},
): AudioWidget = globalWidgetManager.audio(url, setup)

/**
 * Creates an [ImageWidget] from a URL and registers it with the global widget manager.
 *
 * @param url The URL of the image.
 * @param setup A lambda to configure the widget after creation.
 * @return The created and registered [ImageWidget].
 */
public fun imageWidget(
    url: String,
    setup: ImageWidget.() -> Unit = {},
): ImageWidget = globalWidgetManager.image(url, setup)

/**
 * Creates a [VideoWidget] from a URL and registers it with the global widget manager.
 *
 * @param url The URL of the video.
 * @param setup A lambda to configure the widget after creation.
 * @return The created and registered [VideoWidget].
 */
public fun videoWidget(
    url: String,
    setup: VideoWidget.() -> Unit = {},
): VideoWidget = globalWidgetManager.video(url, setup)
