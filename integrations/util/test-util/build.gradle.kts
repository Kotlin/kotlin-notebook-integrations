plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    api(projects.integrations.util.notebookParser)

    api(libs.kotlinx.serialization.json)
    api(libs.test.kotlintest.assertions)
    api(libs.kotlin.test)
    api(libs.kotlin.jupyter.testkit)
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
}
