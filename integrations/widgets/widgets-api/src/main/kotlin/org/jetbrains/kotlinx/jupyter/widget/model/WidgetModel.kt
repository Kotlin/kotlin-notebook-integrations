package org.jetbrains.kotlinx.jupyter.widget.model

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.DateType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.DatetimeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.datetime.TimeType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.BooleanType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.BytesType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.FloatType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType
import org.jetbrains.kotlinx.jupyter.widget.model.types.widget.WidgetReferenceType
import org.jetbrains.kotlinx.jupyter.widget.protocol.Patch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Base class for all widget models.
 * Manages properties, state synchronization, and messaging.
 */
public abstract class WidgetModel(
    protected val widgetManager: WidgetManager,
) {
    private val properties = mutableMapOf<String, WidgetModelProperty<*>>()
    private val changeListeners = mutableListOf<(Patch, Boolean) -> Unit>()
    private val customMessageListeners = mutableListOf<CustomMessageListener>()

    /**
     * Retrieves a property by name.
     */
    public fun getProperty(name: String): WidgetModelProperty<*>? = properties[name]

    /**
     * Returns the full serialized state of all properties.
     */
    public fun getFullState(): Patch = properties.mapValues { (_, property) -> property.serializedValue }

    /**
     * Sends a custom message to the frontend.
     * @param content Message payload.
     * @param metadata Optional message metadata.
     * @param buffers Optional binary buffers.
     */
    public fun sendCustomMessage(
        content: JsonObject,
        metadata: JsonElement? = null,
        buffers: List<ByteArray> = emptyList(),
    ) {
        widgetManager.sendCustomMessage(this, content, metadata, buffers)
    }

    public fun addCustomMessageListener(listener: CustomMessageListener) {
        customMessageListeners.add(listener)
    }

    internal fun handleCustomMessage(
        content: JsonObject,
        metadata: JsonElement?,
        buffers: List<ByteArray>,
    ) {
        for (listener in customMessageListeners) {
            listener(content, metadata, buffers)
        }
    }

    /**
     * Applies a state update received from the frontend.
     */
    public fun applyFrontendPatch(patch: Patch) {
        applyPatchImpl(patch, fromFrontend = true)
    }

    /**
     * Applies a state update locally.
     */
    public fun applyPatch(patch: Patch) {
        applyPatchImpl(patch, fromFrontend = false)
    }

    private fun applyPatchImpl(
        patch: Patch,
        fromFrontend: Boolean,
    ) {
        for ((key, value) in patch) {
            val property = properties[key] ?: continue
            property.applyPatch(value, fromFrontend)
        }
    }

    /**
     * Adds a listener for state changes (both local and from frontend).
     */
    public fun addChangeListener(listener: (Patch, fromFrontend: Boolean) -> Unit) {
        changeListeners.add(listener)
    }

    private fun addProperty(property: WidgetModelProperty<*>) {
        properties[property.name] = property
        property.addChangeListener { patch, fromFrontend ->
            notifyChange(mapOf(property.name to patch), fromFrontend)
        }
    }

    private fun notifyChange(
        patch: Patch,
        fromFrontend: Boolean,
    ) {
        for (listener in changeListeners) {
            listener(patch, fromFrontend)
        }
    }

    /**
     * Creates a property delegate.
     * @param echoUpdate If true, updates from frontend are echoed back.
     *
     * Note: While thread safety is not explicitly guaranteed, property updates
     * from multiple threads should generally be safe as long as the underlying
     * kernel messaging protocol is thread-safe.
     */
    protected fun <T> prop(
        name: String,
        type: WidgetModelPropertyType<T>,
        initialValue: T,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, T> = WidgetKtPropertyDelegate(name, type, initialValue, echoUpdate)

    protected fun stringProp(
        name: String,
        initialValue: String = "",
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, String> = prop(name, StringType, initialValue, echoUpdate)

    protected fun nullableStringProp(
        name: String,
        initialValue: String? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, String?> = prop(name, NullableType(StringType), initialValue, echoUpdate)

    protected fun intProp(
        name: String,
        initialValue: Int = 0,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Int> = prop(name, IntType, initialValue, echoUpdate)

    protected fun nullableIntProp(
        name: String,
        initialValue: Int? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Int?> = prop(name, NullableType(IntType), initialValue, echoUpdate)

    protected fun doubleProp(
        name: String,
        initialValue: Double = 0.0,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Double> = prop(name, FloatType, initialValue, echoUpdate)

    protected fun nullableDoubleProp(
        name: String,
        initialValue: Double? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Double?> = prop(name, NullableType(FloatType), initialValue, echoUpdate)

    protected fun boolProp(
        name: String,
        initialValue: Boolean = false,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Boolean> = prop(name, BooleanType, initialValue, echoUpdate)

    protected fun nullableBoolProp(
        name: String,
        initialValue: Boolean? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Boolean?> = prop(name, NullableType(BooleanType), initialValue, echoUpdate)

    protected fun bytesProp(
        name: String,
        initialValue: ByteArray = byteArrayOf(),
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, ByteArray> = prop(name, BytesType, initialValue, echoUpdate)

    protected fun dateProp(
        name: String,
        initialValue: LocalDate,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, LocalDate> = prop(name, DateType, initialValue, echoUpdate)

    protected fun nullableDateProp(
        name: String,
        initialValue: LocalDate? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, LocalDate?> = prop(name, NullableType(DateType), initialValue, echoUpdate)

    protected fun dateTimeProp(
        name: String,
        initialValue: Instant,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Instant> = prop(name, DatetimeType, initialValue, echoUpdate)

    protected fun nullableDateTimeProp(
        name: String,
        initialValue: Instant? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, Instant?> = prop(name, NullableType(DatetimeType), initialValue, echoUpdate)

    protected fun timeProp(
        name: String,
        initialValue: LocalTime,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, LocalTime> = prop(name, TimeType, initialValue, echoUpdate)

    protected fun nullableTimeProp(
        name: String,
        initialValue: LocalTime? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, LocalTime?> = prop(name, NullableType(TimeType), initialValue, echoUpdate)

    protected fun <M : WidgetModel> widgetProp(
        name: String,
        initialValue: M,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, M> = prop(name, WidgetReferenceType(), initialValue, echoUpdate)

    protected fun <M : WidgetModel> nullableWidgetProp(
        name: String,
        initialValue: M? = null,
        echoUpdate: Boolean = true,
    ): ReadWriteProperty<WidgetModel, M?> = prop(name, NullableType(WidgetReferenceType()), initialValue, echoUpdate)

    protected inner class WidgetKtPropertyDelegate<T>(
        private val property: WidgetModelProperty<T>,
    ) : ReadWriteProperty<WidgetModel, T> {
        internal constructor(name: String, type: WidgetModelPropertyType<T>, initialValue: T, echoUpdate: Boolean = true) :
            this(WidgetModelPropertyImpl(name, type, initialValue, widgetManager, echoUpdate))

        init {
            addProperty(property)
        }

        override fun getValue(
            thisRef: WidgetModel,
            property: KProperty<*>,
        ): T = this.property.value

        override fun setValue(
            thisRef: WidgetModel,
            property: KProperty<*>,
            value: T,
        ) {
            this.property.value = value
        }
    }
}

/**
 * Represents a single property of a widget model.
 */
public interface WidgetModelProperty<T> {
    /**
     * Name of the property as it appears in the Jupyter protocol.
     */
    public val name: String

    /**
     * Type information for serialization and validation.
     */
    public val type: WidgetModelPropertyType<T>

    /**
     * Current value of the property.
     */
    public var value: T

    /**
     * If true, updates from the frontend are echoed back.
     */
    public var echoUpdate: Boolean

    /**
     * Current value serialized for the Jupyter protocol.
     */
    public val serializedValue: Any?

    /**
     * Applies a new value received via the Jupyter protocol.
     */
    public fun applyPatch(
        patch: Any?,
        fromFrontend: Boolean = false,
    )

    /**
     * Adds a listener for changes to this property.
     */
    public fun addChangeListener(listener: (Any?, fromFrontend: Boolean) -> Unit)
}

internal class WidgetModelPropertyImpl<T>(
    override val name: String,
    override val type: WidgetModelPropertyType<T>,
    initialValue: T,
    private val widgetManager: WidgetManager,
    override var echoUpdate: Boolean,
) : WidgetModelProperty<T> {
    private var _value: T = initialValue
    private val listeners = mutableListOf<(newValue: Any?, fromFrontend: Boolean) -> Unit>()

    override var value: T
        get() = _value
        set(newValue) = setNewValue(newValue)

    override val serializedValue: Any? get() = type.serialize(value, widgetManager)

    override fun applyPatch(
        patch: Any?,
        fromFrontend: Boolean,
    ) {
        setNewValue(type.deserialize(patch, widgetManager), fromFrontend)
    }

    override fun addChangeListener(listener: (Any?, Boolean) -> Unit) {
        listeners.add(listener)
    }

    private fun setNewValue(
        newValue: T,
        fromFrontend: Boolean = false,
    ) {
        if (newValue == _value) return
        _value = newValue
        notifyListeners(newValue, fromFrontend)
    }

    private fun notifyListeners(
        newValue: T,
        fromFrontend: Boolean,
    ) {
        val serializedValue = type.serialize(newValue, widgetManager)
        for (listener in listeners) {
            listener(serializedValue, fromFrontend)
        }
    }
}
