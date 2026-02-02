package org.jetbrains.kotlinx.jupyter.test.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommCommunicationFacility
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Test implementation of CommCommunicationFacility that records all sent events.
 */
class TestServerCommCommunicationFacility : CommCommunicationFacility {
    private val eventsQueue = LinkedBlockingQueue<CommEvent>()

    fun getNextEvent(timeoutMs: Long = 5000): CommEvent =
        eventsQueue.poll(timeoutMs, TimeUnit.MILLISECONDS)
            ?: throw AssertionError("No comm event received within ${timeoutMs}ms")

    fun checkNoNextEvent(timeoutMs: Long = 100) {
        val event = eventsQueue.poll(timeoutMs, TimeUnit.MILLISECONDS)
        if (event != null) {
            throw AssertionError("Expected no comm event, but received: $event")
        }
    }

    override val contextMessage: RawMessage? get() = null

    private fun addEvent(event: CommEvent) {
        eventsQueue.add(event)
    }

    override fun sendCommOpen(
        commId: String,
        targetName: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        addEvent(CommEvent.Open(commId, targetName, data, metadata, buffers))
    }

    override fun sendCommMessage(
        commId: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        addEvent(CommEvent.Message(commId, data, metadata, buffers))
    }

    override fun sendCommClose(
        commId: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        addEvent(CommEvent.Close(commId, data, metadata, buffers))
    }

    override fun processCallbacks(action: () -> Unit) {
        action()
    }

    fun reset() {
        eventsQueue.clear()
    }
}
