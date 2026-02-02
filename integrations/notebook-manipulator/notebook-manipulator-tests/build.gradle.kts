plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    testImplementation(projects.integrations.notebookManipulator.notebookManipulatorJupyter)
    testImplementation(projects.integrations.util.testUtil)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.test.kotlintest.assertions)
    testImplementation(libs.kotlinx.serialization.json)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
}
