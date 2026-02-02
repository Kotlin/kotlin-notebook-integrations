# Jupyter Notebooks Parser

[![JetBrains official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin experimental stability](https://img.shields.io/badge/project-experimental-kotlin.svg?colorA=555555&colorB=AC29EC&label=&logo=kotlin&logoColor=ffffff&logoWidth=10)](https://kotlinlang.org/docs/components-stability.html)
[![Maven Central version](https://img.shields.io/maven-central/v/org.jetbrains.kotlinx/jupyter-notebooks-parser?color=blue&label=Maven%20Central)](https://search.maven.org/artifact/org.jetbrains.kotlinx/jupyter-notebooks-parser)

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