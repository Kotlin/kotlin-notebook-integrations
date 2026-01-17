package org.jetbrains.kotlinx.jupyter.widget.protocol

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject

/**
 * Base interface for messages exchanged between the frontend and backend via Comms.
 * These follow the Jupyter Widgets protocol.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("method")
@Serializable
internal sealed interface WidgetMessage

/**
 * Interface for messages that contain binary buffer path information.
 */
internal interface WithBufferPaths {
    /**
     * Paths where binary data should be inserted.
     */
    val bufferPaths: List<List<Any>>
}

/**
 * Interface for messages that contain the widget's property state.
 */
internal interface WidgetStateMessage : WithBufferPaths {
    /**
     * The property state of the widget.
     */
    val state: JsonObject
}

@Serializable
internal class WidgetOpenMessage(
    override val state: JsonObject,
    @SerialName("buffer_paths")
    @Serializable(with = BufferPathsSerializer::class)
    override val bufferPaths: List<List<Any>>,
) : WidgetStateMessage

@Serializable
@SerialName("update")
internal class WidgetUpdateMessage(
    override val state: JsonObject,
    @SerialName("buffer_paths")
    @Serializable(with = BufferPathsSerializer::class)
    override val bufferPaths: List<List<Any>>,
) : WidgetMessage,
    WidgetStateMessage

@Serializable
@SerialName("echo_update")
internal class WidgetEchoUpdateMessage(
    override val state: JsonObject,
    @SerialName("buffer_paths")
    @Serializable(with = BufferPathsSerializer::class)
    override val bufferPaths: List<List<Any>>,
) : WidgetMessage,
    WidgetStateMessage

@Serializable
@SerialName("request_state")
internal class RequestStateMessage : WidgetMessage

@Serializable
@SerialName("custom")
internal class CustomMessage(
    val content: JsonObject,
) : WidgetMessage

@Serializable
@SerialName("request_states")
internal class RequestStatesMessage : WidgetMessage

@Serializable
@SerialName("update_states")
internal class UpdateStatesMessage(
    val states: JsonObject,
    @SerialName("buffer_paths")
    @Serializable(with = BufferPathsSerializer::class)
    override val bufferPaths: List<List<Any>>,
) : WidgetMessage,
    WithBufferPaths
