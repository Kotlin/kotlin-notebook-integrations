package org.jetbrains.kotlinx.jupyter.widget.model

import kotlinx.serialization.Serializable

internal const val DEFAULT_MAJOR_VERSION = 2
internal const val DEFAULT_MINOR_VERSION = 1
internal const val DEFAULT_PATCH_VERSION = 0

internal val versionConstraintRegex = Regex("""\D*(\d+)\.(\d+)""")

/**
 * The set of six immutable properties that define a widget specification
 * for the Jupyter Widgets protocol.
 * These properties identify which frontend model and view should be used.
 */
@Serializable
public data class WidgetSpec(
    /**
     * The name of the frontend model class (e.g., "IntSliderModel").
     */
    val modelName: String,
    /**
     * The name of the NPM module containing the model (e.g., "@jupyter-widgets/controls").
     */
    val modelModule: String,
    /**
     * Semver requirement for the model module.
     */
    val modelModuleVersion: String,
    /**
     * The name of the frontend view class (e.g., "IntSliderView").
     * Can be null for model-only widgets.
     */
    val viewName: String?,
    /**
     * The name of the NPM module containing the view.
     */
    val viewModule: String,
    /**
     * Semver requirement for the view module.
     */
    val viewModuleVersion: String,
)
