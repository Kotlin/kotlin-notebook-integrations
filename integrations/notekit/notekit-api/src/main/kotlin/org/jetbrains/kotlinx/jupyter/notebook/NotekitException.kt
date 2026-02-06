package org.jetbrains.kotlinx.jupyter.notebook

/**
 * Exception thrown when a notebook operation fails.
 */
public class NotekitException(
    message: String,
    public val errorCode: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)
