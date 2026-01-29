package org.jetbrains.kotlinx.jupyter.ktor.client.core

import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readJsonStr

/**
 * If the server returned a JSON response, it can be converted directly to a Kotlin DataFrame using this method.
 *
 * @throws IllegalStateException if the server didn't respond with a `Content-Type: application/json` header.
 * @throws ClassNotFoundException if Kotlin DataFrames are not on the classpath. It can be added using
 * `%use dataframe`. Until KTNB-795 is fixed, it is required to load dataframes before the ktor-client,
 * i.e.,``%use dataframe, ktor-client`.
 */
public fun NotebookHttpResponse.toDataFrame(): DataFrame<*> =
    runBlocking {
        val contentType = ktorResponse.contentType()
        if (contentType == null || !contentType.match(ContentType.Application.Json)) {
            throw IllegalStateException(
                buildString {
                    append("HTTP request did not return JSON, but ")
                    append(contentType)
                    if (contentType != null) {
                        append(" (")
                        append("type = ")
                        append(contentType.contentType)
                        append(", subtype = ")
                        append(contentType.contentSubtype)
                        append(")")
                    }
                },
            )
        }
        val json = ktorResponse.bodyAsText()
        DataFrame.readJsonStr(json)
    }
