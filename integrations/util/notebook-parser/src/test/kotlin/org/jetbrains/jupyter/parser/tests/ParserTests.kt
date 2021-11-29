package org.jetbrains.jupyter.parser.tests

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.jupyter.parser.JupyterParser
import org.jetbrains.jupyter.parser.notebook.Cell
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo

@Suppress("TestFunctionName")
class ParserTests {
    private var testName: String? = null
    @BeforeEach
    fun setup(testInfo: TestInfo) {
        testName = testInfo.testMethod.get().name
    }

    private fun doTest(checker: (JupyterNotebook) -> Unit = {}) {
        val file = myData.resolve("$testName.ipynb")
        val notebook = JupyterParser.parse(file)
        checker(notebook)
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
    fun netflix() = doTest { notebook ->
        notebook.metadata.languageInfo?.name shouldBe "kotlin"
        notebook.cells shouldHaveSize 109
        notebook.cells.filter { it.type == Cell.Type.CODE } shouldHaveSize 57
    }

    @Test
    fun WineNetWithKotlinDL() = doTest()

    companion object {
        private val myData = testDataDir<ParserTests>()
    }
}
