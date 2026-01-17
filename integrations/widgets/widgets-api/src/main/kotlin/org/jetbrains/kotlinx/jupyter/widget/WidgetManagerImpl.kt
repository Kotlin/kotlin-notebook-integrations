package org.jetbrains.kotlinx.jupyter.widget

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.jetbrains.kotlinx.jupyter.api.DisplayResult
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResultEx
import org.jetbrains.kotlinx.jupyter.api.MimeTypes
import org.jetbrains.kotlinx.jupyter.api.libraries.Comm
import org.jetbrains.kotlinx.jupyter.api.libraries.CommManager
import org.jetbrains.kotlinx.jupyter.protocol.api.RawMessage
import org.jetbrains.kotlinx.jupyter.widget.model.DEFAULT_MAJOR_VERSION
import org.jetbrains.kotlinx.jupyter.widget.model.DEFAULT_MINOR_VERSION
import org.jetbrains.kotlinx.jupyter.widget.model.DEFAULT_PATCH_VERSION
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.versionConstraintRegex
import org.jetbrains.kotlinx.jupyter.widget.protocol.CustomMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.Patch
import org.jetbrains.kotlinx.jupyter.widget.protocol.RequestStateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.RequestStatesMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.UpdateStatesMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetEchoUpdateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetOpenMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetStateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetUpdateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.getWireMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPatch

private val widgetOpenMetadataJson =
    buildJsonObject {
        put("version", "${DEFAULT_MAJOR_VERSION}.${DEFAULT_MINOR_VERSION}.${DEFAULT_PATCH_VERSION}")
    }

/**
 * Implementation of [WidgetManager] that interfaces with the [CommManager]
 * to provide real-time synchronization between Kotlin and Jupyter frontend.
 */
public class WidgetManagerImpl(
    private val commManager: CommManager,
    private val classLoaderProvider: () -> ClassLoader,
) : WidgetManager {
    private val widgetTarget = "jupyter.widget"
    private val widgetControlTarget = "jupyter.widget.control"
    private val widgets = mutableMapOf<String, WidgetModel>()
    private val widgetIdByWidget = mutableMapOf<WidgetModel, String>()
    private val commByWidget = mutableMapOf<WidgetModel, Comm>()

    override val factoryRegistry: WidgetFactoryRegistry = WidgetFactoryRegistry()

    override val contextMessage: RawMessage? get() = commManager.contextMessage

    /**
     * Echo update can be enabled via the `JUPYTER_WIDGETS_ECHO` environment variable.
     */
    override var echoUpdateEnabled: Boolean =
        System.getenv("JUPYTER_WIDGETS_ECHO")?.let { it.lowercase() == "true" } ?: false

    init {
        commManager.registerCommTarget(widgetControlTarget) { comm, _, _, _ ->
            comm.onMessage { msg, _, _ ->
                when (Json.decodeFromJsonElement<WidgetMessage>(msg)) {
                    is RequestStatesMessage -> {
                        val fullStates =
                            widgets.mapValues { (id, widget) ->
                                widget.getFullState()
                            }

                        val wireMessage = getWireMessage(fullStates)
                        val message = UpdateStatesMessage(wireMessage.state, wireMessage.bufferPaths)

                        val data = Json.encodeToJsonElement<WidgetMessage>(message).jsonObject
                        comm.send(data, null, wireMessage.buffers)
                    }

                    else -> {}
                }
            }
        }

        commManager.registerCommTarget(widgetTarget) { comm, data, _, buffers ->
            val openMessage = Json.decodeFromJsonElement<WidgetOpenMessage>(data)
            val modelName = openMessage.state["_model_name"]?.jsonPrimitive?.content!!
            val widgetFactory = factoryRegistry.loadWidgetFactory(modelName, classLoaderProvider())

            val widget = widgetFactory.create(this, fromFrontend = true)
            val patch = openMessage.toPatch(buffers)
            widget.applyFrontendPatch(patch)

            initializeWidget(comm, widget)
        }
    }

    override fun getWidget(modelId: String): WidgetModel? = widgets[modelId]

    override fun getWidgetId(widget: WidgetModel): String? = widgetIdByWidget[widget]

    override fun registerWidget(widget: WidgetModel) {
        if (getWidgetId(widget) != null) return

        val fullState = widget.getFullState()
        val wireMessage = getWireMessage(fullState)

        val comm =
            commManager.openComm(
                widgetTarget,
                Json
                    .encodeToJsonElement(
                        WidgetOpenMessage(
                            wireMessage.state,
                            wireMessage.bufferPaths,
                        ),
                    ).jsonObject,
                widgetOpenMetadataJson,
                wireMessage.buffers,
            )

        initializeWidget(comm, widget)
    }

    override fun closeWidget(widget: WidgetModel) {
        val comm = commByWidget[widget] ?: return
        comm.close()
    }

    override fun renderWidget(widget: WidgetModel): DisplayResult =
        MimeTypedResultEx(
            buildJsonObject {
                val modelId = getWidgetId(widget) ?: error("Widget is not registered")
                var versionMajor = DEFAULT_MAJOR_VERSION
                var versionMinor = DEFAULT_MINOR_VERSION
                var modelName: String? = null
                if (widget is DefaultWidgetModel) {
                    modelName = widget.modelName
                    val version = widget.viewModuleVersion
                    val matchResult = versionConstraintRegex.find(version)
                    if (matchResult != null) {
                        versionMajor = matchResult.groupValues[1].toInt()
                        versionMinor = matchResult.groupValues[2].toInt()
                    }
                }
                if (modelName != null) {
                    put(MimeTypes.HTML, "$modelName(id=$modelId)")
                }
                put(
                    "application/vnd.jupyter.widget-view+json",
                    buildJsonObject {
                        put("version_major", versionMajor)
                        put("version_minor", versionMinor)
                        put("model_id", modelId)
                    },
                )
            },
            null,
        )

    override fun sendCustomMessage(
        widget: WidgetModel,
        content: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        val comm = commByWidget[widget] ?: error("Widget is not registered")
        val message = CustomMessage(content)
        val data = Json.encodeToJsonElement<WidgetMessage>(message).jsonObject
        comm.send(data, metadata, buffers)
    }

    private fun initializeWidget(
        comm: Comm,
        widget: WidgetModel,
    ) {
        val modelId = comm.id
        widgetIdByWidget[widget] = modelId
        widgets[modelId] = widget
        commByWidget[widget] = comm

        // Reflect kernel-side changes on the frontend
        widget.addChangeListener { patch, fromFrontend ->
            handleWidgetUpdate(comm, widget, patch, fromFrontend)
        }

        // Reflect frontend-side changes on kernel
        comm.onMessage { msg, metadata, buffers ->
            when (val message = Json.decodeFromJsonElement<WidgetMessage>(msg)) {
                is WidgetStateMessage -> {
                    widget.applyFrontendPatch(message.toPatch(buffers))
                }

                is RequestStateMessage -> {
                    val fullState = widget.getFullState()
                    val wireMessage = getWireMessage(fullState)
                    val data =
                        Json
                            .encodeToJsonElement<WidgetMessage>(
                                WidgetUpdateMessage(
                                    wireMessage.state,
                                    wireMessage.bufferPaths,
                                ),
                            ).jsonObject
                    comm.send(data, null, wireMessage.buffers)
                }

                is CustomMessage -> {
                    widget.handleCustomMessage(
                        message.content,
                        metadata,
                        buffers,
                    )
                }

                else -> {}
            }
        }

        comm.onClose { _, _ ->
            widgets.remove(modelId)
            widgetIdByWidget.remove(widget)
            commByWidget.remove(widget)
        }
    }

    private fun handleWidgetUpdate(
        comm: Comm,
        widget: WidgetModel,
        patch: Patch,
        fromFrontend: Boolean,
    ) {
        val updatePatch =
            if (fromFrontend) {
                if (!echoUpdateEnabled) return
                val echoPatch =
                    patch.filterKeys {
                        widget.getProperty(it)?.echoUpdate != false
                    }
                if (echoPatch.isEmpty()) return
                echoPatch
            } else {
                patch
            }

        val wireMessage = getWireMessage(updatePatch)
        val updateMessage =
            if (fromFrontend) {
                WidgetEchoUpdateMessage(
                    wireMessage.state,
                    wireMessage.bufferPaths,
                )
            } else {
                WidgetUpdateMessage(
                    wireMessage.state,
                    wireMessage.bufferPaths,
                )
            }

        val updateData =
            Json
                .encodeToJsonElement<WidgetMessage>(updateMessage)
                .jsonObject

        comm.send(updateData, null, wireMessage.buffers)
    }
}
