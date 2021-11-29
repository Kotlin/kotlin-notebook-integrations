# Jupyter Notebooks Parser

[![Kotlin](https://img.shields.io/badge/Kotlin-1.6.0-blue.svg?logo=Kotlin)](http://kotlinlang.org)
[![Maven Central version](https://img.shields.io/maven-central/v/org.jetbrains.kotlinx/jupyter-notebooks-parser?color=blue&label=Maven%20Central)](https://repo1.maven.org/maven2/org/jetbrains/kotlinx/jupyter-notebooks-parser/)
[![GitHub License](https://img.shields.io/github/license/ileasile/jupyter-notebooks-parser?color=blue&label=License)](http://www.apache.org/licenses/LICENSE-2.0)

This library simply parses Jupyter Notebook files into POJOs using `kotlinx.serialization`.

Usage:
```kotlin
import org.jetbrains.jupyter.parser.JupyterParser
import java.io.File

val notebook = JupyterParser.parse(File("notebook.ipynb"))
JupyterParser.save(notebook, File("newNotebook.ipynb"))
```
