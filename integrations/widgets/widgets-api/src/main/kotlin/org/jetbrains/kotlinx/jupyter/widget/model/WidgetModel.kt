package org.jetbrains.kotlinx.jupyter.widget.model

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.compound.NullableType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.BooleanType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.BytesType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.FloatType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.IntType
import org.jetbrains.kotlinx.jupyter.widget.model.types.primitive.StringType
import org.jetbrains.kotlinx.jupyter.widget.model.types.widget.WidgetReferenceType
import org.jetbrains.kotlinx.jupyter.widget.protocol.Patch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public abstract class WidgetModel(
    protected val widgetManager: WidgetManager,
) {
    private val properties = mutableMapOf<String, WidgetModelProperty<*>>()
    private val changeListeners = mutableListOf<(Patch, Boolean) -> Unit>()

    public fun getFullState(): Patch = properties.mapValues { (_, property) -> property.serializedValue }

    public fun applyFrontendPatch(patch: Patch) {
        applyPatchImpl(patch, fromFrontend = true)
    }

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

    protected fun <T> prop(
        name: String,
        type: WidgetModelPropertyType<T>,
        initialValue: T,
    ): ReadWriteProperty<WidgetModel, T> = WidgetKtPropertyDelegate(name, type, initialValue)

    protected fun stringProp(
        name: String,
        initialValue: String = "",
    ): ReadWriteProperty<WidgetModel, String> = prop(name, StringType, initialValue)

    protected fun intProp(
        name: String,
        initialValue: Int = 0,
    ): ReadWriteProperty<WidgetModel, Int> = prop(name, IntType, initialValue)

    protected fun doubleProp(
        name: String,
        initialValue: Double = 0.0,
    ): ReadWriteProperty<WidgetModel, Double> = prop(name, FloatType, initialValue)

    protected fun boolProp(
        name: String,
        initialValue: Boolean = false,
    ): ReadWriteProperty<WidgetModel, Boolean> = prop(name, BooleanType, initialValue)

    protected fun bytesProp(
        name: String,
        initialValue: ByteArray = byteArrayOf(),
    ): ReadWriteProperty<WidgetModel, ByteArray> = prop(name, BytesType, initialValue)

    protected fun <M : WidgetModel> widgetProp(
        name: String,
        initialValue: M,
    ): ReadWriteProperty<WidgetModel, M> = prop(name, WidgetReferenceType(), initialValue)

    protected fun <M : WidgetModel> nullableWidgetProp(
        name: String,
        initialValue: M? = null,
    ): ReadWriteProperty<WidgetModel, M?> = prop(name, NullableType(WidgetReferenceType()), initialValue)

    protected inner class WidgetKtPropertyDelegate<T>(
        private val property: WidgetModelProperty<T>,
    ) : ReadWriteProperty<WidgetModel, T> {
        internal constructor(name: String, type: WidgetModelPropertyType<T>, initialValue: T) :
            this(WidgetModelPropertyImpl(name, type, initialValue, widgetManager))

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

public interface WidgetModelProperty<T> {
    public val name: String
    public val type: WidgetModelPropertyType<T>
    public var value: T

    public val serializedValue: Any?

    public fun applyPatch(
        patch: Any?,
        fromFrontend: Boolean = false,
    )

    public fun addChangeListener(listener: (Any?, fromFrontend: Boolean) -> Unit)
}

internal class WidgetModelPropertyImpl<T>(
    override val name: String,
    override val type: WidgetModelPropertyType<T>,
    initialValue: T,
    private val widgetManager: WidgetManager,
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
