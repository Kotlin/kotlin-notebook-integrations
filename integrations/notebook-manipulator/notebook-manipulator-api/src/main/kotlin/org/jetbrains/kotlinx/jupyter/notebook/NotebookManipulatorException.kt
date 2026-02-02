package org.jetbrains.kotlinx.jupyter.notebook

/**
 * Exception thrown when a notebook manipulation operation fails.
 */
public class NotebookManipulatorException(
    message: String,
    public val errorCode: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
