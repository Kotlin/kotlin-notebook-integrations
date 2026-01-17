package org.jetbrains.kotlinx.jupyter.widget.model.types.compound

import org.jetbrains.kotlinx.jupyter.widget.WidgetManager
import org.jetbrains.kotlinx.jupyter.widget.model.types.AbstractWidgetModelPropertyType
import org.jetbrains.kotlinx.jupyter.widget.model.types.WidgetModelPropertyType

public class PairType<A, B>(
    public val firstType: WidgetModelPropertyType<A>,
    public val secondType: WidgetModelPropertyType<B>,
) : AbstractWidgetModelPropertyType<Pair<A, B>>("pair<${firstType.name}, ${secondType.name}>") {
    override val default: Pair<A, B> = firstType.default to secondType.default

    override fun serialize(
        propertyValue: Pair<A, B>,
        widgetManager: WidgetManager,
    ): List<Any?> =
        listOf(
            firstType.serialize(propertyValue.first, widgetManager),
            secondType.serialize(propertyValue.second, widgetManager),
        )

    override fun deserialize(
        patchValue: Any?,
        widgetManager: WidgetManager,
    ): Pair<A, B> {
        require(patchValue is List<*> && patchValue.size == 2) {
            "Expected List of size 2 for $name, got $patchValue"
        }
        return firstType.deserialize(patchValue[0], widgetManager) to
            secondType.deserialize(patchValue[1], widgetManager)
    }
}
