package org.jetbrains.kotlinx.jupyter.widget.generator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

internal const val WIDGETS_PACKAGE: String = "org.jetbrains.kotlinx.jupyter.widget"
internal const val WIDGET_TYPES_PACKAGE: String = "$WIDGETS_PACKAGE.model.types"
internal const val WIDGET_LIBRARY_PACKAGE: String = "$WIDGETS_PACKAGE.library"

internal data class EnumInfo(
    val className: String,
    val values: List<String>,
)

internal interface PropertyType {
    val kotlinType: String
    val typeExpression: String
    val isNullable: Boolean
    val imports: Set<String> get() = emptySet()
    val helperDeclarations: List<String> get() = emptyList()

    fun getDefaultValueExpression(defaultValue: JsonElement): String

    val delegateName: String? get() = null

    val kotlinTypeWithoutNullability: String get() = kotlinType.removeSuffix("?")
}

private open class BasicPropertyType(
    override val kotlinType: String,
    override val typeExpression: String,
    override val imports: Set<String>,
    val nonNullableDelegateName: String? = null,
    val nullableDelegateName: String? = null,
) : PropertyType {
    override val isNullable: Boolean get() = false
    override val delegateName: String? get() = if (isNullable) nullableDelegateName else nonNullableDelegateName

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = renderLiteral(kotlinType, defaultValue)
}

private open class PrimitiveType(
    kotlinType: String,
    typeName: String,
    delegateName: String? = null,
    nullableDelegateName: String? = null,
) : BasicPropertyType(
        kotlinType = kotlinType,
        typeExpression = typeName,
        imports = setOf("$WIDGET_TYPES_PACKAGE.primitive.$typeName"),
        nonNullableDelegateName = delegateName,
        nullableDelegateName = nullableDelegateName,
    )

private object StringPropertyType : PrimitiveType("String", "StringType", "stringProp", "nullableStringProp")

private object BooleanPropertyType : PrimitiveType("Boolean", "BooleanType", "boolProp", "nullableBoolProp")

private object IntPropertyType : PrimitiveType("Int", "IntType", "intProp", "nullableIntProp")

private object DoublePropertyType : PrimitiveType("Double", "FloatType", "doubleProp", "nullableDoubleProp")

private object BytesPropertyType : PrimitiveType("ByteArray", "BytesType", "bytesProp")

private object AnyPropertyType : PrimitiveType("Any", "AnyType")

private open class DatetimeBasePropertyType(
    kotlinType: String,
    typeName: String,
) : BasicPropertyType(
        kotlinType = kotlinType,
        typeExpression = typeName,
        imports = setOf("$WIDGET_TYPES_PACKAGE.datetime.$typeName"),
    )

private object DatetimePropertyType : DatetimeBasePropertyType("java.time.Instant", "DatetimeType")

private object DatePropertyType : DatetimeBasePropertyType("java.time.LocalDate", "DateType")

private object TimePropertyType : DatetimeBasePropertyType("java.time.LocalTime", "TimeType")

private object RawObjectPropertyType : BasicPropertyType(
    kotlinType = "Map<String, Any?>",
    typeExpression = "RawObjectType",
    imports = setOf("$WIDGET_TYPES_PACKAGE.compound.RawObjectType"),
)

private class NullablePropertyType(
    private val inner: PropertyType,
) : PropertyType {
    override val kotlinType: String get() = inner.kotlinType + if (inner.isNullable) "" else "?"
    override val typeExpression: String get() = if (inner.isNullable) inner.typeExpression else "NullableType(${inner.typeExpression})"
    override val isNullable: Boolean get() = true
    override val imports: Set<String> get() = inner.imports + "$WIDGET_TYPES_PACKAGE.compound.NullableType"
    override val helperDeclarations: List<String> get() = inner.helperDeclarations

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = inner.getDefaultValueExpression(defaultValue)

    override val delegateName: String? get() = (inner as? BasicPropertyType)?.nullableDelegateName
}

private class ArrayPropertyType(
    attribute: AttributeSchema,
    json: Json,
    enums: MutableMap<String, EnumInfo>,
) : PropertyType {
    private val elementType =
        run {
            val actualItems = attribute.items ?: AttributeItems(type = AttributeType.Single("any"))
            val elementSchema = AttributeSchema(name = "item", type = actualItems.type, default = JsonNull, widget = actualItems.widget)
            elementSchema.toPropertyType(json, enums)
        }

    override val kotlinType: String get() = "List<${elementType.kotlinType}>"
    override val typeExpression: String get() = "ArrayType(${elementType.typeExpression})"
    override val isNullable: Boolean get() = false
    override val imports: Set<String> get() = elementType.imports + "$WIDGET_TYPES_PACKAGE.compound.ArrayType"
    override val helperDeclarations: List<String> get() = elementType.helperDeclarations

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = defaultArrayValue(elementType.kotlinType, defaultValue)
}

private class EnumPropertyType(
    val enumName: String,
    private val enumValues: List<String>,
    defaultValue: JsonElement,
) : PropertyType {
    private val defaultEntry = (defaultValue as? JsonPrimitive)?.content?.takeUnless { it == "null" }

    private val defaultExpression =
        when {
            defaultEntry == null || defaultEntry !in enumValues -> "null"
            defaultEntry.isEmpty() -> "$enumName.Default"
            else -> "$enumName.${defaultEntry.toPascalCase()}"
        }

    private val typeDefaultExpression =
        run {
            val typeDefaultEntry = defaultEntry?.takeIf { it in enumValues } ?: enumValues.first()
            val typeDefaultEntryName = typeDefaultEntry.ifEmpty { "Default" }
            "$enumName.${typeDefaultEntryName.toPascalCase()}"
        }

    override val kotlinType: String get() = "WidgetEnumEntry<$enumName>"
    override val typeExpression: String get() = "WidgetEnumType($enumName, $typeDefaultExpression)"
    override val isNullable: Boolean get() = false
    override val imports: Set<String>
        get() =
            setOf(
                "$WIDGET_LIBRARY_PACKAGE.enums.$enumName",
                "$WIDGET_TYPES_PACKAGE.enums.WidgetEnumEntry",
                "$WIDGET_TYPES_PACKAGE.enums.WidgetEnumType",
            )

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = defaultExpression
}

private class ReferencePropertyType(
    widget: String,
    private val targetClass: String =
        when (widget) {
            "Widget" -> "WidgetModel"
            "Axis" -> "ControllerAxisWidget"
            else -> if (widget.endsWith("Widget")) widget else "${widget}Widget"
        },
) : BasicPropertyType(
        kotlinType = "$targetClass?",
        typeExpression = "NullableType(WidgetReferenceType<$targetClass>())",
        imports =
            buildSet {
                add("$WIDGET_TYPES_PACKAGE.compound.NullableType")
                add("$WIDGET_TYPES_PACKAGE.widget.WidgetReferenceType")
                if (targetClass == "WidgetModel") {
                    add("$WIDGETS_PACKAGE.model.WidgetModel")
                }
            },
        nonNullableDelegateName = "widgetProp",
        nullableDelegateName = "nullableWidgetProp",
    ) {
    override val isNullable: Boolean get() = true

    override fun getDefaultValueExpression(defaultValue: JsonElement): String =
        if ((defaultValue as? JsonPrimitive)?.content == "reference to new instance") {
            "if (fromFrontend) null else widgetManager.${targetClass.toCamelCase().removeSuffix("Widget")}()"
        } else {
            "null"
        }
}

private class UnionPropertyType(
    attribute: AttributeSchema,
    json: Json,
    enums: MutableMap<String, EnumInfo>,
) : PropertyType {
    private val name = attribute.name
    private val options: List<PropertyType>
    private val defaultValueExpression: String

    init {
        val optionSchemas =
            if (attribute.unionAttributes.isNotEmpty()) {
                attribute.unionAttributes.map { json.decodeFromJsonElement<AttributeSchema>(it.addMissingFields(attribute)) }
            } else {
                when (val t = attribute.type) {
                    is AttributeType.Single -> listOf(attribute.copy(type = t))
                    is AttributeType.Union -> t.options.map { opt -> attribute.copy(type = opt) }
                }
            }

        options = optionSchemas.map { it.toPropertyType(json, enums) }
        defaultValueExpression = options.first().getDefaultValueExpression(attribute.default)
    }

    private val nameForType = "${name.toPascalCase()}UnionType"

    override val kotlinType: String get() = "Any"
    override val typeExpression: String get() = nameForType
    override val isNullable: Boolean get() = false
    override val imports: Set<String> get() = options.flatMap { it.imports }.toSet() + "$WIDGET_TYPES_PACKAGE.compound.UnionType"

    override val helperDeclarations: List<String>
        get() {
            val helpers = options.flatMap { it.helperDeclarations }.toMutableList()

            val deserializers = options.joinToString(", ") { it.typeExpression }
            val unionDeclaration =
                """
                |private val $nameForType = UnionType<Any>(
                |    name = "$name",
                |    default = $defaultValueExpression,
                |    serializerSelector = { value ->
                |        when (value) {
                ${
                    options.joinToString("\n") { opt ->
                        val checkType =
                            opt.kotlinTypeWithoutNullability.let {
                                if (it.contains("<")) it.replace(Regex("<.*>"), "<*>") else it
                            }
                        "|            is $checkType -> ${opt.typeExpression}"
                    }
                }
                |            else -> ${options.last().typeExpression}
                |        }
                |    },
                |    deserializers = listOf($deserializers),
                |)
                """.trimMargin()
            helpers.add(unionDeclaration)
            return helpers
        }

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = defaultValueExpression
}

internal fun AttributeSchema.toPropertyType(
    json: Json,
    enums: MutableMap<String, EnumInfo>,
): PropertyType {
    val isNullable = allowNone || default is JsonNull
    val baseType =
        when (type) {
            is AttributeType.Single -> {
                if (enum.isNotEmpty()) {
                    val enumName = name.toPascalCase()
                    enums.getOrPut(enumName) { EnumInfo(enumName, enum) }.also {
                        if (it.values != enum) error("Enum conflict for $enumName")
                    }

                    EnumPropertyType(enumName, enum, default)
                } else {
                    when (type.name) {
                        "string" -> StringPropertyType
                        "bool" -> BooleanPropertyType
                        "int" -> IntPropertyType
                        "float" -> DoublePropertyType
                        "bytes" -> BytesPropertyType
                        "any" -> AnyPropertyType
                        "Datetime" -> DatetimePropertyType
                        "Date" -> DatePropertyType
                        "Time" -> TimePropertyType
                        "reference" -> ReferencePropertyType(widget ?: error("Reference widget is not specified"))
                        "object" -> RawObjectPropertyType
                        "array" -> ArrayPropertyType(this, json, enums)
                        else -> error("Unsupported type ${type.name}")
                    }
                }
            }
            is AttributeType.Union -> UnionPropertyType(this, json, enums)
        }
    if (isNullable) {
        if (baseType.typeExpression == "AnyType") {
            return object : PropertyType by baseType {
                override val kotlinType: String get() = "Any?"
                override val isNullable: Boolean get() = true
            }
        }
        return NullablePropertyType(baseType)
    }
    return baseType
}

private fun JsonObject.addMissingFields(base: AttributeSchema): JsonObject {
    val newMap = toMutableMap()
    if ("name" !in newMap) newMap["name"] = JsonPrimitive(base.name)
    if ("default" !in newMap) newMap["default"] = base.default
    if ("help" !in newMap) newMap["help"] = JsonPrimitive(base.help)
    return JsonObject(newMap)
}

private fun renderLiteral(
    kotlinType: String,
    value: JsonElement,
): String {
    val isNullable = kotlinType.endsWith("?")
    return when {
        value is JsonNull -> "null"
        kotlinType.startsWith("ByteArray") -> "byteArrayOf()"
        kotlinType.startsWith("Map<") -> {
            if (isNullable) "null" else "emptyMap()"
        }
        value is JsonPrimitive -> {
            if (value.isString) {
                val quoted =
                    value.content
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                "\"$quoted\""
            } else {
                val primitiveValue =
                    value.booleanOrNull
                        ?: value.intOrNull
                        ?: value.doubleOrNull
                        ?: error("Unsupported primitive value for $kotlinType: $value")
                primitiveValue.toString()
            }
        }
        else -> error("Unsupported literal for $kotlinType: $value")
    }
}

private fun defaultArrayValue(
    elementKotlinType: String,
    value: JsonElement,
): String =
    if (value is kotlinx.serialization.json.JsonArray && value.isNotEmpty()) {
        val renderedItems = value.joinToString(", ") { renderLiteral(elementKotlinType, it) }
        "listOf($renderedItems)"
    } else {
        "emptyList()"
    }
