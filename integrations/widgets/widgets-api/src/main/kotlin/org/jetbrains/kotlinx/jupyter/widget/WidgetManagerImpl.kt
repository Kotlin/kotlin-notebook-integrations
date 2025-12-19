package org.jetbrains.kotlinx.jupyter.widget

import kotlinx.serialization.json.Json
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
import org.jetbrains.kotlinx.jupyter.widget.model.DEFAULT_MAJOR_VERSION
import org.jetbrains.kotlinx.jupyter.widget.model.DEFAULT_MINOR_VERSION
import org.jetbrains.kotlinx.jupyter.widget.model.DefaultWidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetFactoryRegistry
import org.jetbrains.kotlinx.jupyter.widget.model.WidgetModel
import org.jetbrains.kotlinx.jupyter.widget.model.versionConstraintRegex
import org.jetbrains.kotlinx.jupyter.widget.protocol.CustomMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.RequestStateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.RequestStatesMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.UpdateStatesMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetOpenMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetStateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.WidgetUpdateMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.getWireMessage
import org.jetbrains.kotlinx.jupyter.widget.protocol.toPatch

private val widgetOpenMetadataJson =
    buildJsonObject {
        put("version", "${DEFAULT_MAJOR_VERSION}.${DEFAULT_MINOR_VERSION}")
    }

public class WidgetManagerImpl(
    private val commManager: CommManager,
    private val classLoaderProvider: () -> ClassLoader,
) : WidgetManager {
    private val widgetTarget = "jupyter.widget"
    private val widgetControlTarget = "jupyter.widget.control"
    private val widgets = mutableMapOf<String, WidgetModel>()
    private val widgetIdByWidget = mutableMapOf<WidgetModel, String>()

    override val factoryRegistry: WidgetFactoryRegistry = WidgetFactoryRegistry()

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
                        comm.send(data, null, emptyList())
                    }

                    else -> {}
                }
            }
        }

        commManager.registerCommTarget(widgetTarget) { comm, data, _, buffers ->
            val openMessage = Json.decodeFromJsonElement<WidgetOpenMessage>(data)
            val modelName = openMessage.state["_model_name"]?.jsonPrimitive?.content!!
            val widgetFactory = factoryRegistry.loadWidgetFactory(modelName, classLoaderProvider())

            val widget = widgetFactory.create(this)
            val patch = openMessage.toPatch(buffers)
            widget.applyPatch(patch)

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

    override fun renderWidget(widget: WidgetModel): DisplayResult =
        MimeTypedResultEx(
            buildJsonObject {
                val modelId = getWidgetId(widget) ?: error("Widget is not registered")
                var versionMajor = DEFAULT_MAJOR_VERSION
                var versionMinor = DEFAULT_MINOR_VERSION
                var modelName: String? = null
                if (widget is DefaultWidgetModel) {
                    modelName = widget.modelName
                    val version = widget.modelModuleVersion
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

    private fun initializeWidget(
        comm: Comm,
        widget: WidgetModel,
    ) {
        val modelId = comm.id
        widgetIdByWidget[widget] = modelId
        widgets[modelId] = widget

        // Reflect kernel-side changes on the frontend
        widget.addChangeListener { patch ->
            val wireMessage = getWireMessage(patch)
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

        // Reflect frontend-side changes on kernel
        comm.onMessage { msg, _, buffers ->
            when (val message = Json.decodeFromJsonElement<WidgetMessage>(msg)) {
                is WidgetStateMessage -> {
                    widget.applyPatch(message.toPatch(buffers))
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

                is CustomMessage -> {}

                else -> {}
            }
        }
    }
}
