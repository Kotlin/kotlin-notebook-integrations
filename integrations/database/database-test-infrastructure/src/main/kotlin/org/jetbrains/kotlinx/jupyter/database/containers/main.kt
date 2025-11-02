package org.jetbrains.kotlinx.jupyter.database.containers

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

const val DEFAULT_PORT = org.jetbrains.kotlinx.jupyter.database.gen.BuildConfig.TEST_SERVER_PORT

fun main(args: Array<String>) {
    val port = args.indexOf("--port").let { if (it >= 0) args.getOrNull(it + 1)?.toInt() ?: DEFAULT_PORT else DEFAULT_PORT }
    val manager = ContainerManager()
    val server =
        embeddedServer(CIO, port = port) {
            install(IgnoreTrailingSlash)
            install(ContentNegotiation) {
                json()
            }

            routing {
                get("/start/{containerId}") {
                    val container = call.parameters["containerId"] ?: error("containerId not specified")
                    val id = ContainerId(container)
                    if (!manager.isSupported(id)) {
                        call.respond(HttpStatusCode.NotFound, "Container not supported: $container")
                    } else {
                        runCatching { manager.start(id) }
                            .onSuccess { call.respond(it.info()) }
                            .onFailure { call.respond(HttpStatusCode.InternalServerError, it.stackTraceToString()) }
                    }
                }

                get("/stop/{containerId}") {
                    val container = call.parameters["containerId"] ?: error("containerId not specified")
                    val id = ContainerId(container)
                    runCatching { manager.stop(id) }
                        .onSuccess { stopped -> call.respond(mapOf(id.container to stopped)) }
                        .onFailure { call.respond(HttpStatusCode.InternalServerError, it.stackTraceToString()) }
                }
            }
        }

    Runtime.getRuntime().addShutdownHook(Thread { manager.stopAll() })
    server.start(wait = true)
}
