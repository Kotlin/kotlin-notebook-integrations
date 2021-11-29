package org.jetbrains.jupyter.parser

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import java.io.File

object JupyterParser {
    private val parser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun parse(text: String): JupyterNotebook {
        return parser.decodeFromString(text)
    }

    fun parse(file: File): JupyterNotebook {
        return parse(file.readText())
    }

    fun saveToJson(notebook: JupyterNotebook): String {
        return parser.encodeToString(notebook)
    }

    fun save(notebook: JupyterNotebook, file: File) {
        file.writeText(saveToJson(notebook))
    }
}
