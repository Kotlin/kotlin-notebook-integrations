package org.jetbrains.kotlinx.jupyter.database.org.jetbrains.kotlinx.jupyter.database.test.helpers

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import jupyter.kotlin.DependsOn
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.jupyter.api.EmbeddedKernelRunMode
import org.jetbrains.kotlinx.jupyter.api.StandaloneKernelRunMode
import org.jetbrains.kotlinx.jupyter.config.DefaultKernelLoggerFactory
import org.jetbrains.kotlinx.jupyter.config.defaultRepositoriesCoordinates
import org.jetbrains.kotlinx.jupyter.libraries.DefaultResolutionInfoProviderFactory
import org.jetbrains.kotlinx.jupyter.libraries.createLibraryHttpUtil
import org.jetbrains.kotlinx.jupyter.libraries.getStandardResolver
import org.jetbrains.kotlinx.jupyter.repl.ReplForJupyter
import org.jetbrains.kotlinx.jupyter.repl.creating.createRepl
import org.jetbrains.kotlinx.jupyter.repl.embedded.NoOpInMemoryReplResultsHolder
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

/**
 * Helper for creating a Spring application file in the specified [tempDir].
 * It is assumed the test will cleanup the file after use.
 */
fun createSpringApplicationFile(
    tempDir: Path,
    fileName: String,
    fileBody: () -> String,
): Path {
    val tempFile = Files.createFile(tempDir.resolve(fileName))
    tempFile.writeText(fileBody())
    return tempFile
}

fun startTestContainer(containerId: String): DatabaseInfo {
    val testPort = org.jetbrains.kotlinx.jupyter.database.gen.BuildConfig.TEST_SERVER_PORT
    return runBlocking {
        // Port is defined in `gradle.properties`
        val response = httpClient.get("http://127.0.0.1:$testPort/start/$containerId")
        if (response.status.isSuccess()) {
            response.body<DatabaseInfo>()
        } else {
            error("Failed to start container $containerId [${response.status}]: ${response.body<String>()}")
        }
    }
}

fun stopTestContainer(containerId: String) {
    val testPort = org.jetbrains.kotlinx.jupyter.database.gen.BuildConfig.TEST_SERVER_PORT
    runBlocking {
        val response = httpClient.get("http://127.0.0.1:$testPort/stop/$containerId")
        if (!response.status.isSuccess()) {
            error("Failed to stop container $containerId [${response.status}]: ${response.body<String>()}")
        }
    }
}

private val httpClient =
    HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            // HTTP Test Server might need to download a docker image in this time-frame,
            // so leave room for that to happen
            requestTimeoutMillis = 120_000
            socketTimeoutMillis = 120_000
        }
    }

/** Response from Test Server when starting a container */
@Serializable
data class DatabaseInfo(
    val type: String,
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
)

/**
 * Copy of the test [ReplProvider], but using [StandaloneKernelRunMode] rather
 * than [EmbeddedKernelRunMode]. It also sets up a standard resolver that can
 * load maven dependencies.
 */
fun interface StandardReplProvider {
    operator fun invoke(classpath: List<File>): ReplForJupyter

    companion object {
        private val httpUtil = createLibraryHttpUtil(DefaultKernelLoggerFactory)

        fun withStandardResolver(
            shouldResolve: (String?) -> Boolean = { true },
            shouldResolveToEmpty: (String?) -> Boolean = { false },
        ) = ReplProvider { classpath ->
            val standardResolutionInfoProvider =
                DefaultResolutionInfoProviderFactory.create(
                    httpUtil,
                    DefaultKernelLoggerFactory,
                )
            val resolver =
                getStandardResolver(
                    standardResolutionInfoProvider,
                    httpUtil.httpClient,
                    httpUtil.libraryDescriptorsManager,
                )
            createRepl(
                httpUtil = httpUtil,
                scriptClasspath = classpath,
                kernelRunMode = StandaloneKernelRunMode,
                mavenRepositories = defaultRepositoriesCoordinates,
                libraryResolver = resolver,
                inMemoryReplResultsHolder = NoOpInMemoryReplResultsHolder,
            ).apply {
                initializeWithCurrentClasspath()
            }
        }

        @Suppress("unused")
        fun forLibrariesTesting(libraries: Collection<String>): ReplProvider =
            withStandardResolver(
                shouldResolveToEmpty = { it in libraries },
            )

        private fun ReplForJupyter.initializeWithCurrentClasspath() {
            eval { librariesScanner.addLibrariesFromClassLoader(currentClassLoader, this, notebook) }
        }

        @Suppress("unused")
        private val currentClassLoader = DependsOn::class.java.classLoader
    }
}
