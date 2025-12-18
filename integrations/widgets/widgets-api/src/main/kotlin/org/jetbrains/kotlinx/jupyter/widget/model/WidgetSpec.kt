package org.jetbrains.kotlinx.jupyter.widget.model

import kotlinx.serialization.Serializable

internal const val DEFAULT_MAJOR_VERSION = 2
internal const val DEFAULT_MINOR_VERSION = 0
internal const val DEFAULT_PATCH_VERSION = 0

private const val DEFAULT_VERSION_CONSTRAINT = "^$DEFAULT_MAJOR_VERSION.$DEFAULT_MINOR_VERSION.$DEFAULT_PATCH_VERSION"
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
    val viewName: String,
    val viewModule: String,
    val viewModuleVersion: String,
)

public fun controlsSpec(
    controlName: String,
    versionConstraint: String = DEFAULT_VERSION_CONSTRAINT,
): WidgetSpec = iPyWidgetsSpec(controlName, "@jupyter-widgets/controls", versionConstraint)

public fun baseSpec(
    controlName: String,
    versionConstraint: String = DEFAULT_VERSION_CONSTRAINT,
): WidgetSpec = iPyWidgetsSpec(controlName, "@jupyter-widgets/base", versionConstraint)

public fun outputSpec(
    controlName: String,
    versionConstraint: String = DEFAULT_VERSION_CONSTRAINT,
): WidgetSpec = iPyWidgetsSpec(controlName, "@jupyter-widgets/output", versionConstraint)

public fun iPyWidgetsSpec(
    controlName: String,
    moduleName: String,
    versionConstraint: String = DEFAULT_VERSION_CONSTRAINT,
): WidgetSpec =
    WidgetSpec(
        modelName = controlName + "Model",
        modelModule = moduleName,
        modelModuleVersion = versionConstraint,
        viewName = controlName + "View",
        viewModule = moduleName,
        viewModuleVersion = versionConstraint,
    )
