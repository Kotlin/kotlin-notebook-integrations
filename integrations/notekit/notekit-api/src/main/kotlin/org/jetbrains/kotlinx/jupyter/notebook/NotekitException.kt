package org.jetbrains.kotlinx.jupyter.notebook

/**
 * Exception thrown when a notebook operation fails.
 */
public class NotekitException(
    message: String,
    public val errorCode: NotekitErrorCode? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Error codes for notebook operations.
 */
public enum class NotekitErrorCode {
    /** Unknown or unspecified error */
    UNKNOWN,

    /** Request timed out */
    TIMEOUT,

    /** Invalid parameters provided */
    INVALID_PARAMS,

    /** Invalid range or index */
    INVALID_RANGE,

    /** Invalid response format */
    INVALID_RESPONSE,

    /** Unknown comm error */
    COMM_ERROR,

    ;

    public companion object {
        public fun fromString(code: String): NotekitErrorCode =
            try {
                NotekitErrorCode.valueOf(code)
            } catch (_: IllegalArgumentException) {
                UNKNOWN
            }
    }
}
