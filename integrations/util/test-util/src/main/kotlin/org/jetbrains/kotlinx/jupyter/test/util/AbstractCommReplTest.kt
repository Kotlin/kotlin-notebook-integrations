package org.jetbrains.kotlinx.jupyter.test.util

import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.protocol.comms.CommManagerImpl
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider

/**
 * Base class for REPL tests that use Comm communication.
 *
 * Provides common utilities for testing Comm-based integrations.
 */
abstract class AbstractCommReplTest(
    protected val facility: TestServerCommCommunicationFacility,
    protected val commManager: CommManagerImpl,
    provider: ReplProvider,
) : JupyterReplTestCase(provider) {
    protected var nextEventIndex: Int = 0

    /**
     * Clears all recorded events and resets the event index.
     */
    protected open fun resetEvents() {
        facility.sentEvents.clear()
        nextEventIndex = 0
    }

    /**
     * Verifies that the next event is a CommOpen event.
     */
    protected open fun shouldHaveNextOpenEvent(expectedTargetName: String): CommEvent.Open =
        shouldHaveOpenEvent(nextEventIndex++, expectedTargetName)

    /**
     * Verifies that the event at the specified index is a CommOpen event.
     */
    protected open fun shouldHaveOpenEvent(
        index: Int,
        expectedTargetName: String,
    ): CommEvent.Open {
        val openEvent = facility.sentEvents[index].shouldBeInstanceOf<CommEvent.Open>()
        return openEvent
    }

    /**
     * Sends a comm message to the specified comm.
     */
    protected fun sendCommMessage(
        commId: String,
        data: JsonObject,
        buffers: List<ByteArray> = emptyList(),
    ) {
        commManager.processCommMessage(commId, data, null, buffers)
    }

    /**
     * Builds a JsonObject with the specified key-value pairs.
     */
    protected fun buildState(vararg state: Pair<String, Any?>) = buildJsonObject { putState(*state) }

    /**
     * Puts the specified key-value pairs into the JsonObjectBuilder.
     */
    private fun JsonObjectBuilder.putState(vararg state: Pair<String, Any?>) {
        for ((key, value) in state) {
            when (value) {
                is String -> put(key, value)
                is Number -> put(key, value)
                is Boolean -> put(key, value)
                is JsonElement -> put(key, value)
                null -> put(key, null as String?)
                else -> throw IllegalArgumentException("Unsupported type: ${value::class}")
            }
        }
    }
}
