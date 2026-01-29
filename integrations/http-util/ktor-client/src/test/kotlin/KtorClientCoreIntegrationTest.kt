package org.jetbrains.kotlinx.jupyter.ktor.client.test

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonElement
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.jupyter.repl.result.EvalResultEx
import org.jetbrains.kotlinx.jupyter.testkit.JupyterReplTestCase
import org.jetbrains.kotlinx.jupyter.testkit.ReplProvider
import kotlin.test.Test

class KtorClientCoreIntegrationTest :
    JupyterReplTestCase(
        replProvider = ReplProvider.withDefaultClasspathResolution(),
    ) {
    @Test
    fun `default client engine`() {
        val engineName = execRaw("http.ktorClient.engine")?.javaClass?.simpleName
        engineName shouldBe "CIOEngine"
        val engineName2 = execRaw("io.ktor.client.HttpClient().engine")?.javaClass?.simpleName
        engineName2 shouldBe "CIOEngine"
    }

    @Test
    fun `calls compilation`() {
        val exec =
            execRaw(
                """
                import io.ktor.client.request.*
                
                lazy { 
                    http.get("https://example.org").body<String>()
                    http.get("https://example.org") {
                        header("Authorization", "Basic 123")
                        parameter("param", "value")
                    }.bodyAsText()
                    http.post("https://example.org") {
                        header("Authorization", "Basic 123")
                        setBody("body")
                    }.readBytes()
                }
                """.trimIndent(),
            )
        // checking only compilation, so that the test doesn't involve actual network calls
        exec.shouldBeInstanceOf<Lazy<*>>()
    }

    @Test
    fun `mock calls`() {
        @Language("JSON")
        val json = """{"b":"b","a":{"b":"b","a":null}}"""
        execRaw(
            """
            $MOCK_CLIENT_IMPORTS
            @Serializable
            data class A(val b: String, val a: A?)
            ${createMockClient(json)}
            """.trimIndent(),
        )

        val response1 = execRaw("""client.get("https://example.org").bodyAsText()""")
        response1 shouldBe json

        val response2 = execRaw("""client.get("https://example.org").body<String>()""")
        response2 shouldBe json

        val response3 = execRaw("""client.get("https://example.org").body<JsonElement>()""")
        response3.shouldBeInstanceOf<JsonElement>()
        response3.toString() shouldBe json

        val response4 = execRaw("""client.get("https://example.org").body<A>()""")
        response4.toString() shouldBe "A(b=b, a=A(b=b, a=null))"

        val response5 = execRaw("""client.get("https://example.org").readBytes()""")
        response5.shouldBeInstanceOf<ByteArray>()
        response5.toString(Charsets.UTF_8) shouldBe json
    }

    @Test
    fun `create dataframe from response`() {
        val json = """[{"a": 1}, {"a": 2}, {"a": 3}]"""
        execRaw(
            """
            $MOCK_CLIENT_IMPORTS
            @Serializable
            data class A(val b: String, val a: A?)
            ${createMockClient(json)}
            """.trimIndent(),
        )
        val response = execRaw("""client.get("https://example.org").toDataFrame()""")
        response.shouldBeInstanceOf<DataFrame<*>>()
    }

    @Test
    fun `cannot create dataframe from response that doesn't return json`() {
        execRaw(
            """
            $MOCK_CLIENT_IMPORTS
            ${createMockClient("error", HttpStatusCode.InternalServerError)}
            """.trimIndent(),
        )
        val res = execEx("""client.get("https://example.org").toDataFrame()""")
        res.shouldBeInstanceOf<EvalResultEx.Error>()
        res.error.cause.shouldBeInstanceOf<IllegalStateException>()
    }

    @Test
    fun `wrong content type exception should provide additional info`() {
        val json = """[{"a": 1}, {"a": 2}, {"a": 3}]"""

        execRaw(
            """
            $MOCK_CLIENT_IMPORTS
            ${createMockClient(json, contentType = "text/plain")}
            """.trimIndent(),
        )
        val res = execEx("""client.get("https://example.org").toDataFrame()""")
        res.shouldBeInstanceOf<EvalResultEx.Error>()
        val cause = res.error.cause.shouldNotBeNull()
        cause.shouldBeInstanceOf<IllegalStateException>()
        cause.message shouldContain "(type = text, subtype = plain)"
    }
}
