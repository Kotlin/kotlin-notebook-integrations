package org.jetbrains.jupyter.parser.tests

import io.kotest.matchers.shouldBe
import org.jetbrains.jupyter.parser.JupyterParser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@Suppress("TestFunctionName")
class ParseSaveTests {
    private var testName: String? = null
    @BeforeEach
    fun setup(testInfo: TestInfo) {
        testName = testInfo.testMethod.get().name
    }

    private fun doTest() {
        val file = myData.resolve("$testName.ipynb")

        val notebook1 = JupyterParser.parse(file)
        val json1 = JupyterParser.saveToJson(notebook1)

        val notebook2 = JupyterParser.parse(json1)
        val json2 = JupyterParser.saveToJson(notebook2)

        json1 shouldBe json2
    }

    @Test
    fun DeepLearning4j() = doTest()

    @Test
    fun `DeepLearning4j-Cuda`() = doTest()

    @Test
    fun Gral() = doTest()

    @Test
    fun KotlinNumpy() = doTest()

    @Test
    fun Krangl() = doTest()

    @Test
    fun LetsPlot() = doTest()

    @Test
    fun `Resources Example`() = doTest()

    @Test
    fun Titanic() = doTest()

    @Test
    fun Titanic2() = doTest()

    @Test
    fun `40 puzzles`() = doTest()

    @Test
    fun github() = doTest()

    @Test
    fun movies() = doTest()

    @Test
    fun netflix() = doTest()

    @Test
    fun WineNetWithKotlinDL() = doTest()

    companion object {
        private val myData = testDataDir<ParserTests>()
    }
}
