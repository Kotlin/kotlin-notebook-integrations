plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.test.kotlintest.assertions)
    api(libs.kotlin.test)
    api(libs.kotlin.jupyter.testkit)
    api(libs.jupyter.notebooks.parser)
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
}
