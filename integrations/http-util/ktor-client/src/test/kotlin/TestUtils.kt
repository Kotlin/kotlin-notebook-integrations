package org.jetbrains.kotlinx.jupyter.ktor.client.test

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode

const val MOCK_CLIENT_IMPORTS = """
    import io.ktor.client.engine.mock.MockEngine
    import io.ktor.client.engine.mock.respond
    import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
    import io.ktor.http.ContentType
    import io.ktor.http.HttpHeaders
    import io.ktor.http.HttpStatusCode
    import io.ktor.http.headersOf
    import io.ktor.serialization.kotlinx.json.json
    import org.jetbrains.kotlinx.jupyter.ktor.client.core.NotebookHttpClient
"""

fun createMockClient(
    jsonResponse: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    contentType: String = ContentType.Application.Json.toString(),
): String =
    """
    val client = NotebookHttpClient(MockEngine) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            addHandler {
                respond(
                    content = ""${'"'}$jsonResponse""${'"'},
                    status = HttpStatusCode.fromValue(${status.value}),
                    headers = headersOf(HttpHeaders.ContentType, "$contentType")
                )
            }
        }
    }
    """.trimIndent()
