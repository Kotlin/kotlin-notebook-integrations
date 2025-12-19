plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    implementation(projects.integrations.widgets.widgetsApi)
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
    explicitApi()
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.kotlinx.jupyter.widget.integration.WidgetJupyterIntegration")
}

kotlinPublications {
    publication {
        description.set("Kotlin Jupyter kernel integration for IPython Widgets")
    }
}
