package org.jetbrains.kotlinx.jupyter.widget.test

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MediaWidgetTest : AbstractWidgetReplTest() {
    @Test
    fun `audio widget from url`() {
        execRaw("val url = \"https://example.com/audio.mp3\"")
        execRaw("val a = audioWidget(url)")

        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("AudioModel")

        execRaw("a.value") shouldBe "https://example.com/audio.mp3".encodeToByteArray()
        execRaw("a.format") shouldBe "url"
    }

    @Test
    fun `image widget from url`() {
        execRaw("val url = \"https://example.com/image.png\"")
        execRaw("val i = imageWidget(url)")

        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("ImageModel")

        execRaw("i.value") shouldBe "https://example.com/image.png".encodeToByteArray()
        execRaw("i.format") shouldBe "url"
    }

    @Test
    fun `video widget from url`() {
        execRaw("val url = \"https://example.com/video.mp4\"")
        execRaw("val v = videoWidget(url)")

        shouldHaveNextOpenEvent("LayoutModel")
        shouldHaveNextOpenEvent("VideoModel")

        execRaw("v.value") shouldBe "https://example.com/video.mp4".encodeToByteArray()
        execRaw("v.format") shouldBe "url"
    }

    @Test
    fun `media widget url property`() {
        execRaw("val a = audioWidget()")
        execRaw("val mw: MediaWidget = a")
        execRaw("mw.url = \"https://example.com/test.mp3\"")

        execRaw("a.value") shouldBe "https://example.com/test.mp3".encodeToByteArray()
        execRaw("a.format") shouldBe "url"
        execRaw("mw.url") shouldBe "https://example.com/test.mp3"

        execRaw("a.format = \"mp3\"")
        execRaw("mw.url") shouldBe null
    }
}
