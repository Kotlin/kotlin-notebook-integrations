package org.jetbrains.kotlinx.jupyter.widget.library.media

/**
 * Interface for widgets that represent media content (audio, image, video).
 */
public interface MediaWidget {
    /**
     * The media data as a memory view of bytes.
     */
    public var value: ByteArray

    /**
     * The format of the media.
     */
    public var format: String
}
