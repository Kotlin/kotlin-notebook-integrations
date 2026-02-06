import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

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

    pom {
        githubRepo("Kotlin", "kotlin-notebook-integrations")
        inceptionYear.set("2026")
        licenses {
            apache2()
        }
        developers {
            developer("ileasile", "Ilya Muradyan", "Ilya.Muradyan@jetbrains.com")
        }
    }
}
