# Jupyter Notebooks Parser

[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.6.0-blue.svg?logo=Kotlin)](http://kotlinlang.org)
[![Maven Central version](https://img.shields.io/maven-central/v/org.jetbrains.kotlinx/jupyter-notebooks-parser?color=blue&label=Maven%20Central)](https://search.maven.org/artifact/org.jetbrains.kotlinx/jupyter-notebooks-parser)
[![GitHub License](https://img.shields.io/github/license/ileasile/jupyter-notebooks-parser?color=blue&label=License)](http://www.apache.org/licenses/LICENSE-2.0)

This library simply parses Jupyter Notebook files into POJOs using `kotlinx.serialization`.

Usage:
```kotlin
import org.jetbrains.jupyter.parser.JupyterParser
import java.io.File

val notebook = JupyterParser.parse(File("notebook.ipynb"))
JupyterParser.save(notebook, File("newNotebook.ipynb"))
```

Set it up with Gradle:
```kotlin
dependencies {
    implementation("org.jetbrains.kotlinx:jupyter-notebooks-parser:0.1.0-dev-1")
}
```