plugins {
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jupyter.api)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.publisher)
}

kotlin {
    jvmToolchain(
        libs.versions.jvmTarget
            .get()
            .toInt(),
    )
}

dependencies {
    implementation(libs.snakeYaml)
    api(libs.hikari)
    api(libs.kotlin.jupyter.lib)
    testImplementation(kotlin("test"))
    testImplementation(libs.test.kotlintest.assertions)
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.kotlinx.jupyter.database.internal.DatabaseJupyterIntegration")
}

tasks.test {
    useJUnitPlatform()
}

kotlinPublications {
    publication {
        publicationName.set("database")
        description.set("Kotlin Jupyter kernel integration with DBMSs")
    }
}
