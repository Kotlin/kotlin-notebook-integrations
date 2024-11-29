package org.jetbrains.jupyter.parser

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.jupyter.parser.notebook.JupyterNotebook
import java.io.File

public object JupyterParser {
    private val parser = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    public fun parse(text: String): JupyterNotebook {
        return parser.decodeFromString(text)
    }

    public fun parse(file: File): JupyterNotebook {
        return parse(file.readText())
    }

    public fun saveToJson(notebook: JupyterNotebook): String {
        return parser.encodeToString(notebook)
    }

    public fun save(notebook: JupyterNotebook, file: File) {
        file.writeText(saveToJson(notebook))
    }
}
