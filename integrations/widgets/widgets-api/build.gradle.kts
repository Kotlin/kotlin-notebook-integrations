plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
}

tasks.register<JavaExec>("generateWidgetModels") {
    group = "generation"
    description = "Fetches the ipywidgets model schema and generates Kotlin descriptions for available widget models."

    val outputDir = layout.buildDirectory.dir("generated/widgetModels")

    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jetbrains.kotlinx.jupyter.widget.generation.WidgetModelGeneratorRunner")
    args(outputDir.map { it.asFile.absolutePath })
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
    explicitApi()
}

kotlinPublications {
    publication {
        description.set("Kotlin APIs for IPython Widgets")
    }
}
