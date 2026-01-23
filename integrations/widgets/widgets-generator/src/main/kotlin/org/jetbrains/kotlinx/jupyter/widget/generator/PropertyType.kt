package org.jetbrains.kotlinx.jupyter.widget.generator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlin.reflect.KClass

internal const val WIDGETS_PACKAGE: String = "org.jetbrains.kotlinx.jupyter.widget"
internal const val WIDGET_TYPES_PACKAGE: String = "$WIDGETS_PACKAGE.model.types"
internal const val WIDGET_LIBRARY_PACKAGE: String = "$WIDGETS_PACKAGE.library"

private val typeArgumentsRegex = Regex("<.*>")

private val assignedPropertyTypes =
    listOf(
        AssignedPropertyType("IntRangeSliderWidget", "value", NullablePropertyType(IntRangePropertyType)),
        AssignedPropertyType("FloatRangeSliderWidget", "value", NullablePropertyType(FloatRangePropertyType)),
        AssignedPropertyType("SelectionRangeSliderWidget", "index", NullablePropertyType(IntRangePropertyType)),
    ) +
        listOf(
            "IntsInputWidget" to IntPropertyType,
            "FloatsInputWidget" to DoublePropertyType,
            "TagsInputWidget" to StringPropertyType,
            "ColorsInputWidget" to StringPropertyType,
        ).flatMap { (widgetName, type) ->
            listOf("value", "allowed_tags").map { attributeName ->
                AssignedPropertyType(
                    widgetName,
                    attributeName,
                    ArrayPropertyType(type),
                )
            }
        }

internal data class EnumInfo(
    val className: String,
    val values: List<String>,
)

/**
 * Represents a property type in the widget generator.
 * Maps Jupyter widget attributes to Kotlin types and property delegates.
 */
internal interface PropertyType {
    val kotlinType: String
    val typeExpression: String
    val isNullable: Boolean
    val imports: Set<String> get() = emptySet()
    val helperDeclarations: List<String> get() = emptyList()
    val isEnum: Boolean get() = false
    val enumName: String get() = ""
    val enumValues: List<String> get() = emptyList()
    val optionName: String

    fun getDefaultValueExpression(defaultValue: JsonElement): String

    val delegateName: String? get() = null

    val checkTypeExpression: String
        get() {
            val typeWithoutNullability = kotlinType.removeSuffix("?")
            return typeWithoutNullability.replace(typeArgumentsRegex, "<*>")
        }
}

private open class BasicPropertyType(
    override val kotlinType: String,
    override val typeExpression: String,
    override val imports: Set<String>,
    override val optionName: String = kotlinType,
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
    nonNullableDelegateName: String? = null,
    nullableDelegateName: String? = null,
) : BasicPropertyType(
        kotlinType = kotlinType,
        typeExpression = typeName,
        imports = setOf("$WIDGET_TYPES_PACKAGE.primitive.$typeName"),
        nonNullableDelegateName = nonNullableDelegateName,
        nullableDelegateName = nullableDelegateName,
    )

private object StringPropertyType : PrimitiveType(
    kotlinType = "String",
    typeName = "StringType",
    nonNullableDelegateName = "stringProp",
    nullableDelegateName = "nullableStringProp",
)

private object BooleanPropertyType : PrimitiveType(
    kotlinType = "Boolean",
    typeName = "BooleanType",
    nonNullableDelegateName = "boolProp",
    nullableDelegateName = "nullableBoolProp",
)

private object IntPropertyType : PrimitiveType(
    kotlinType = "Int",
    typeName = "IntType",
    nonNullableDelegateName = "intProp",
    nullableDelegateName = "nullableIntProp",
) {
    override val checkTypeExpression: String get() = "Number"
}

private object DoublePropertyType : PrimitiveType(
    kotlinType = "Double",
    typeName = "FloatType",
    nonNullableDelegateName = "doubleProp",
    nullableDelegateName = "nullableDoubleProp",
) {
    override val checkTypeExpression: String get() = "Number"
}

private object BytesPropertyType : PrimitiveType(
    kotlinType = "ByteArray",
    typeName = "BytesType",
    nonNullableDelegateName = "bytesProp",
) {
    override val optionName: String get() = "Bytes"
}

private object AnyPropertyType : PrimitiveType(
    kotlinType = "Any?",
    typeName = "AnyType",
) {
    override val isNullable: Boolean get() = true
}

private object IntRangePropertyType : BasicPropertyType(
    kotlinType = "IntRange",
    typeExpression = "IntRangeType",
    imports = setOf("$WIDGET_TYPES_PACKAGE.ranges.IntRangeType"),
)

private object FloatRangePropertyType : BasicPropertyType(
    kotlinType = "ClosedRange<Double>",
    typeExpression = "FloatRangeType",
    imports = setOf("$WIDGET_TYPES_PACKAGE.ranges.FloatRangeType"),
)

private open class DatetimeBasePropertyType(
    kotlinType: String,
    typeName: String,
    nonNullableDelegateName: String,
    nullableDelegateName: String,
    optionName: String,
) : BasicPropertyType(
        kotlinType = kotlinType,
        typeExpression = typeName,
        imports = setOf("$WIDGET_TYPES_PACKAGE.datetime.$typeName"),
        optionName = optionName,
        nonNullableDelegateName = nonNullableDelegateName,
        nullableDelegateName = nullableDelegateName,
    )

private object DatetimePropertyType : DatetimeBasePropertyType(
    kotlinType = "java.time.Instant",
    typeName = "DatetimeType",
    nonNullableDelegateName = "dateTimeProp",
    nullableDelegateName = "nullableDateTimeProp",
    optionName = "Datetime",
)

private object DatePropertyType : DatetimeBasePropertyType(
    kotlinType = "java.time.LocalDate",
    typeName = "DateType",
    nonNullableDelegateName = "dateProp",
    nullableDelegateName = "nullableDateProp",
    optionName = "Date",
)

private object TimePropertyType : DatetimeBasePropertyType(
    kotlinType = "java.time.LocalTime",
    typeName = "TimeType",
    nonNullableDelegateName = "timeProp",
    nullableDelegateName = "nullableTimeProp",
    optionName = "Time",
)

private object RawObjectPropertyType : BasicPropertyType(
    kotlinType = "Map<String, Any?>",
    typeExpression = "RawObjectType",
    imports = setOf("$WIDGET_TYPES_PACKAGE.compound.RawObjectType"),
    optionName = "Object",
)

private class NullablePropertyType(
    val inner: PropertyType,
) : PropertyType {
    init {
        require(!inner.isNullable)
    }

    override val kotlinType: String get() = inner.kotlinType + "?"
    override val typeExpression: String get() = "NullableType(${inner.typeExpression})"
    override val isNullable: Boolean get() = true
    override val optionName: String get() = inner.optionName
    override val imports: Set<String> get() = inner.imports + "$WIDGET_TYPES_PACKAGE.compound.NullableType"
    override val helperDeclarations: List<String> get() = inner.helperDeclarations
    override val isEnum: Boolean get() = inner.isEnum

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = inner.getDefaultValueExpression(defaultValue)

    override val delegateName: String? get() = (inner as? BasicPropertyType)?.nullableDelegateName
}

private class ArrayPropertyType(
    private val elementType: PropertyType,
) : PropertyType {
    override val kotlinType: String get() = "List<${elementType.kotlinType}>"
    override val typeExpression: String get() = "ArrayType(${elementType.typeExpression})"
    override val isNullable: Boolean get() = false
    override val optionName: String get() = "List"
    override val imports: Set<String> get() = elementType.imports + "$WIDGET_TYPES_PACKAGE.compound.ArrayType"
    override val helperDeclarations: List<String> get() = elementType.helperDeclarations

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = defaultArrayValue(elementType.kotlinType, defaultValue)
}

private class EnumPropertyType(
    override val enumName: String,
    override val enumValues: List<String>,
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
    override val isEnum: Boolean get() = true
    override val optionName: String get() = enumName
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
            else -> widget.toWidgetClassName()
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
        optionName = targetClass,
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

private fun getEnumObjectName(
    enumValue: String,
    enumName: String,
): String {
    val entryName = if (enumValue.isEmpty()) "Default" else enumValue.toPascalCase()
    return "$entryName$enumName"
}

private class UnionPropertyType(
    attribute: AttributeSchema,
    json: Json,
    enums: MutableMap<String, EnumInfo>,
    namePrefix: String,
) : PropertyType {
    private val name = attribute.name
    private val unionName = "${namePrefix}${name.toPascalCase()}"
    private val unionTypeName = "${unionName}Type"
    private val options: List<PropertyType>
    private val optionNames: List<String>
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

        options = optionSchemas.map { it.toPropertyType(json, enums, namePrefix, skipEnumRegistration = true) }

        val names = options.map { it.optionName + "Value" }
        optionNames =
            if (names.distinct().size == names.size) {
                names
            } else {
                options.indices.map { "${names[it]}$it" }
            }

        val firstOpt = options.first()
        defaultValueExpression =
            if (firstOpt is EnumPropertyType) {
                val defaultEntry = (attribute.default as? JsonPrimitive)?.content?.takeUnless { it == "null" }
                val entryName =
                    if (defaultEntry == null || defaultEntry !in firstOpt.enumValues) {
                        if (firstOpt.enumValues.isEmpty()) {
                            "Default"
                        } else {
                            firstOpt.enumValues
                                .first()
                                .ifEmpty { "Default" }
                                .toPascalCase()
                        }
                    } else {
                        defaultEntry.ifEmpty { "Default" }.toPascalCase()
                    }
                "$unionName.$entryName${firstOpt.enumName}"
            } else {
                val firstOptionDefaultValue = firstOpt.getDefaultValueExpression(attribute.default)
                "$unionName.${optionNames.first()}($firstOptionDefaultValue)"
            }
    }

    override val kotlinType: String get() = unionName
    override val typeExpression: String get() = unionTypeName
    override val isNullable: Boolean get() = false
    override val optionName: String get() = "Union"
    override val imports: Set<String> get() =
        options.flatMap { if (it is EnumPropertyType) emptySet() else it.imports }.toSet() +
            "$WIDGET_TYPES_PACKAGE.compound.UnionType" +
            "$WIDGETS_PACKAGE.protocol.RawPropertyValue"

    override val helperDeclarations: List<String>
        get() {
            val helpers = options.flatMap { if (it is EnumPropertyType) emptyList() else it.helperDeclarations }.toMutableList()

            val interfaceDeclaration =
                buildString {
                    appendLine("public sealed interface $unionName {")
                    options.forEachIndexed { i, opt ->
                        if (opt is EnumPropertyType) {
                            opt.enumValues.forEach { enumValue ->
                                appendLine("    public object ${getEnumObjectName(enumValue, opt.enumName)} : $unionName")
                            }
                        } else {
                            appendLine(
                                "    @JvmInline public value class ${optionNames[i]}(public val value: ${opt.kotlinType}) : $unionName",
                            )
                        }
                    }
                    appendLine("}")
                }
            helpers.add(interfaceDeclaration)

            val typeVarNames = options.indices.map { i -> "${unionTypeName}_Option$i" }
            options.forEachIndexed { i, opt ->
                if (opt !is EnumPropertyType) {
                    helpers.add("private val ${typeVarNames[i]} = ${opt.typeExpression}")
                }
            }

            val serializer =
                buildString {
                    appendLine("{ value, widgetManager ->")
                    appendLine("        when (value) {")
                    options.forEachIndexed { i, opt ->
                        if (opt is EnumPropertyType) {
                            opt.enumValues.forEach { enumValue ->
                                appendLine(
                                    "            is $unionName.${getEnumObjectName(
                                        enumValue,
                                        opt.enumName,
                                    )} -> RawPropertyValue.StringValue(\"$enumValue\")",
                                )
                            }
                        } else {
                            appendLine(
                                "            is $unionName.${optionNames[i]} -> ${typeVarNames[i]}.serialize(value.value, widgetManager)",
                            )
                        }
                    }
                    appendLine("        }")
                    append("    }")
                }

            val deserializers =
                options.indices.joinToString(",\n") { i ->
                    val opt = options[i]
                    if (opt is EnumPropertyType) {
                        val cases =
                            opt.enumValues.joinToString("\n") { enumValue ->
                                "is RawPropertyValue.StringValue -> " +
                                    "if (patch.value == \"$enumValue\") $unionName.${getEnumObjectName(
                                        enumValue,
                                        opt.enumName,
                                    )} else null"
                            }
                        $$"""
                        |        { patch, _ ->
                        |            val res = when (patch) {
                        |                $$cases
                        |                else -> null
                        |            }
                        |            res ?: throw Exception("Unknown enum value: $patch")
                        |        }
                        """.trimMargin()
                    } else {
                        "        { patch, widgetManager -> $unionName.${optionNames[i]}(${typeVarNames[i]}.deserialize(patch, widgetManager)) }"
                    }
                }

            val unionDeclaration =
                """
                |private val $unionTypeName = UnionType<$unionName>(
                |    name = "$name",
                |    default = $defaultValueExpression,
                |    serializer = $serializer,
                |    deserializers = listOf(
                |$deserializers
                |    ),
                |)
                """.trimMargin()
            helpers.add(unionDeclaration)
            return helpers
        }

    override fun getDefaultValueExpression(defaultValue: JsonElement): String = defaultValueExpression
}

/**
 * Converts an [AttributeSchema] to a [PropertyType].
 * This involves determining the appropriate Kotlin type, default value, and imports.
 *
 * @param json The [Json] instance for decoding type attributes.
 * @param enums A map to collect enum definitions discovered during conversion.
 * @param namePrefix Prefix for generated union class names (usually the widget class name).
 * @param skipEnumRegistration If true, discovered enums won't be added to [enums].
 */
internal fun AttributeSchema.toPropertyType(
    json: Json,
    enums: MutableMap<String, EnumInfo>,
    namePrefix: String,
    skipEnumRegistration: Boolean = false,
): PropertyType {
    val isNullable = allowNone

    assignedPropertyTypes
        .find { it.widgetNamePredicate(namePrefix) && it.attributeName == name }
        ?.propertyType
        ?.let { return it }

    val baseType =
        when (type) {
            is AttributeType.Single -> {
                if (enum.isNotEmpty()) {
                    val enumName = name.toPascalCase()
                    if (!skipEnumRegistration) {
                        enums.getOrPut(enumName) { EnumInfo(enumName, enum) }.also {
                            if (it.values != enum) error("Enum conflict for $enumName")
                        }
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
                        "array" -> {
                            val actualItems = this.items ?: AttributeItems(type = AttributeType.Single("any"))
                            val elementSchema =
                                AttributeSchema(name = "item", type = actualItems.type, default = JsonNull, widget = actualItems.widget)
                            val elementType = elementSchema.toPropertyType(json, enums, namePrefix)
                            ArrayPropertyType(elementType)
                        }
                        else -> error("Unsupported type ${type.name}")
                    }
                }
            }
            is AttributeType.Union -> UnionPropertyType(this, json, enums, namePrefix)
        }
    if (isNullable) {
        return baseType.asNullable()
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

private fun PropertyType.asNullable(): PropertyType = if (isNullable) this else NullablePropertyType(this)

private fun renderLiteral(
    kotlinType: String,
    value: JsonElement,
): String {
    val isNullable = kotlinType.endsWith("?")
    return when {
        value is JsonNull -> "null"
        kotlinType == "IntRange" -> {
            renderRangeLiteral(value, Int::class)
        }
        kotlinType == "ClosedRange<Double>" -> {
            renderRangeLiteral(value, Double::class)
        }
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
                val rendered = primitiveValue.toString()
                if (kotlinType == "Double" && "." !in rendered) "$rendered.0" else rendered
            }
        }
        else -> error("Unsupported literal for $kotlinType: $value")
    }
}

private fun defaultArrayValue(
    elementKotlinType: String,
    value: JsonElement,
): String =
    if (value is JsonArray && value.isNotEmpty()) {
        val renderedItems = value.joinToString(", ") { renderLiteral(elementKotlinType, it) }
        "listOf($renderedItems)"
    } else {
        "emptyList()"
    }

private fun renderRangeLiteral(
    jsonValue: JsonElement,
    rangeEndType: KClass<*>,
): String {
    fun renderElement(element: JsonElement): String =
        when (rangeEndType) {
            Int::class -> element.toString()
            Double::class -> element.toString().let { if ("." !in it) "$it.0" else it }
            else -> error("Unsupported range end type: $rangeEndType")
        }

    return if (jsonValue is JsonArray && jsonValue.size == 2) {
        "${renderElement(jsonValue[0])}..${renderElement(jsonValue[1])}"
    } else {
        val renderedElement = renderElement(JsonPrimitive(0))
        "$renderedElement..$renderedElement"
    }
}

private data class AssignedPropertyType(
    val widgetNamePredicate: (String) -> Boolean,
    val attributeName: String,
    val propertyType: PropertyType,
) {
    constructor(
        widgetName: String,
        attributeName: String,
        propertyType: PropertyType,
    ) : this({ it == widgetName }, attributeName, propertyType)
    constructor(
        attributeName: String,
        propertyType: PropertyType,
    ) : this({ true }, attributeName, propertyType)
}
