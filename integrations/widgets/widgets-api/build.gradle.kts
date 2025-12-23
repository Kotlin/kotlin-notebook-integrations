plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publisher)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.jupyter.api)
}

dependencies {
    compileOnly(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
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

kotlinPublications {
    publication {
        description.set("Kotlin APIs for IPython Widgets")
    }
}
