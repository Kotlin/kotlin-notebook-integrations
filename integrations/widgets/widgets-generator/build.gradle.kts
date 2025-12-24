plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
}

kotlin {
    jvmToolchain {
        languageVersion.set(
            JavaLanguageVersion.of(
                JavaVersion
                    .current()
                    .majorVersion
                    .toInt(),
            ),
        )
    }
    explicitApi()
}

tasks.register<JavaExec>("generateWidgets") {
    group = "generation"
    description = "Generate widget models from schema.json"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.jetbrains.kotlinx.jupyter.widget.generator.WidgetGeneratorKt")
}
