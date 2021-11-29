package org.jetbrains.jupyter.parser.tests

import io.kotest.matchers.shouldBe
import org.jetbrains.jupyter.parser.JupyterParser
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.util.stream.Stream
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension

@Suppress("TestFunctionName")
class ParseSaveTests {
    @ParameterizedTest(name = ARGUMENTS_PLACEHOLDER)
    @MethodSource("notebookNames")
    fun doTest(notebookName: String) {
        val file = myData.resolve("$notebookName.ipynb")

        val notebook1 = JupyterParser.parse(file)
        val json1 = JupyterParser.saveToJson(notebook1)

        val notebook2 = JupyterParser.parse(json1)
        val json2 = JupyterParser.saveToJson(notebook2)

        json1 shouldBe json2
    }

    companion object {
        private val myData = testDataDir<ParserTests>()

        @JvmStatic
        fun notebookNames(): Stream<String> {
            return Files.walk(myData.toPath(), 1)
                .filter { it.extension == "ipynb" }
                .map { it.nameWithoutExtension }
        }
    }
}
