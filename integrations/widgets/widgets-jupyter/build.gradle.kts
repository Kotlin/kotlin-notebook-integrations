plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    api(projects.integrations.widgets.widgetsApi)
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
    explicitApi()
}

sourceSets {
    main {
        kotlin.srcDir("src/generated/kotlin")
    }
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.kotlinx.jupyter.widget.integration.WidgetJupyterIntegration")
}

kotlinPublications {
    publication {
        description.set("Kotlin Jupyter kernel integration for IPython Widgets")
    }
}
