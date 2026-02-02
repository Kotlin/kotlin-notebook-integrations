rootProject.name = "kotlin-notebook-integrations"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

projectStructure {
    folder("integrations") {
        folder("http-util") {
            project("ktor-client-core")
            project("ktor-client")
            project("json2kt")
            project("serialization")
        }
        folder("database") {
            project("database-api")
            project("database-test-infrastructure")
            project("database-integration-tests")
        }
        project("intellij-platform")
        folder("widgets") {
            project("widgets-api")
            project("widgets-jupyter")
            project("widgets-generator")
            project("widgets-tests")
        }
        folder("util") {
            project("test-util")
            project("notebook-parser")
        }
    }
}

fun Settings.projectStructure(configuration: ProjectStructure.() -> Unit) {
    val structure = ProjectStructure()
    structure.configuration()
    structure.applyTo(this)
}

class ProjectStructure {
    private val folders = mutableListOf<Folder>()

    fun folder(
        name: String,
        configuration: Folder.() -> Unit,
    ) {
        val folder = Folder(name)
        folder.configuration()
        folders.add(folder)
    }

    fun applyTo(settings: Settings) {
        folders.forEach { it.applyTo(settings, "") }
    }

    class Folder(
        private val name: String,
    ) {
        private val subFolders = mutableListOf<Folder>()
        private val projects = mutableListOf<String>()

        fun folder(
            name: String,
            configuration: Folder.() -> Unit,
        ) {
            val subFolder = Folder(name)
            subFolder.configuration()
            subFolders.add(subFolder)
        }

        fun project(name: String) {
            projects.add(name)
        }

        fun applyTo(
            settings: Settings,
            path: String,
        ) {
            val currentPath = if (path.isEmpty()) name else "$path:$name"
            projects.forEach { settings.include("$currentPath:$it") }
            subFolders.forEach { it.applyTo(settings, currentPath) }
        }
    }
}
