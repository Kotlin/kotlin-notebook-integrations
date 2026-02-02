plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.publisher)
}

kotlin {
    jvmToolchain(
        libs.versions.jvm.toolchain
            .get()
            .toInt(),
    )
    explicitApi()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.test.junit.params)
    testImplementation(libs.test.kotlintest.assertions)
}

kotlinPublications {
    defaultArtifactIdPrefix.set("")

    publication {
        publicationName.set("jupyter-notebooks-parser")
        description.set("Jupyter Notebooks parser and Kotlin utilities for them")
    }
}
