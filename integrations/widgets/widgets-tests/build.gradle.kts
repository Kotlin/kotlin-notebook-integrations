plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.integrations.widgets.widgetsJupyter)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.test.kotlintest.assertions)
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
