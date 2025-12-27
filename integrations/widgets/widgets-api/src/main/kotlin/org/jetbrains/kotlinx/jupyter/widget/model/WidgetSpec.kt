package org.jetbrains.kotlinx.jupyter.widget.model

import kotlinx.serialization.Serializable

internal const val DEFAULT_MAJOR_VERSION = 2
internal const val DEFAULT_MINOR_VERSION = 0

internal val versionConstraintRegex = Regex("""\D*(\d+)\.(\d+)""")

/**
 * The set of six immutable properties that define a widget specification.
 * They should be present in any widget and be distinct across all widgets.
 */
@Serializable
public data class WidgetSpec(
    val modelName: String,
    val modelModule: String,
    val modelModuleVersion: String,
    val viewName: String?,
    val viewModule: String,
    val viewModuleVersion: String,
)
