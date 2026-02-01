package org.jetbrains.kotlinx.jupyter.test.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommCommunicationFacility

/**
 * Test implementation of CommCommunicationFacility that records all sent events.
 */
class TestServerCommCommunicationFacility : CommCommunicationFacility {
    private val events = mutableListOf<CommEvent>()

    val sentEvents: MutableList<CommEvent> get() = events

    override val contextMessage: RawMessage? get() = null

    override fun sendCommOpen(
        commId: String,
        targetName: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        events.add(CommEvent.Open(commId, targetName, data, metadata, buffers))
    }

    override fun sendCommMessage(
        commId: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        events.add(CommEvent.Message(commId, data, metadata, buffers))
    }

    override fun sendCommClose(
        commId: String,
        data: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        events.add(CommEvent.Close(commId, data, metadata, buffers))
    }

    override fun processCallbacks(action: () -> Unit) {
        action()
    }
}
