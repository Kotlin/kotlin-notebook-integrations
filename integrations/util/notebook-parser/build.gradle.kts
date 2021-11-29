import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versions)
    alias(libs.plugins.kover)
}

group = "org.jetbrains"
version = "0.1.0"

kotlin {
    explicitApi = ExplicitApiMode.Strict
}

ktlint {
    version.set(libs.versions.ktlint)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.serializationJson)

    testImplementation(libs.test.junit.engine)
    testImplementation(libs.test.junit.api)
    testImplementation(libs.test.junit.params)
    testImplementation(libs.test.kotlintest.assertions)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
}
