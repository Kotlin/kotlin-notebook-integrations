# Jupyter Notebooks Parser

[![Kotlin](https://img.shields.io/badge/kotlin-1.6.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/github/license/ileasile/jupyter-notebooks-parser?color=blue)](http://www.apache.org/licenses/LICENSE-2.0)

This library simply parses Jupyter Notebook files into POJOs using `kotlinx.serialization`.

Usage:
```kotlin
import org.jetbrains.jupyter.parser.JupyterParser
import java.io.File

val notebook = JupyterParser.parse(File("notebook.ipynb"))
```
