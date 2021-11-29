package org.jetbrains.jupyter.parser

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import java.io.File

object JupyterParser {
    val parser = Json {
        ignoreUnknownKeys = true
    }

    fun parse(text: String): JupyterNotebook {
        return parser.decodeFromString(text)
    }

    fun parse(file: File): JupyterNotebook {
        return parse(file.readText())
    }
}
