import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jupyter.api)
    alias(libs.plugins.publisher)
    alias(libs.plugins.intellij.platform.base)
}

val spaceUsername: String by properties
val spaceToken: String by properties

kotlinJupyter {
    addApiDependency()
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.kotlinx.jupyter.intellij.IntelliJPlatformJupyterIntegration")
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    api(libs.kotlin.jupyter.lib)
//    api(libs.dataframe.core)
    implementation(libs.intellij.structure.ide)
    implementation(libs.intellij.pluginRepositoryRestClient)
    testImplementation(kotlin("test"))

    intellijPlatform {
        intellijIdea(libs.versions.intellijPlatform) {
            useInstaller = false
        }
        bundledPlugin("intellij.jupyter")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(
        libs.versions.jvmTarget
            .get()
            .toInt(),
    )
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-integrations")

        inceptionYear.set("2024")
        licenses {
            apache2()
        }
        developers {
            developer("ileasile", "Ilya Muradyan", "Ilya.Muradyan@jetbrains.com")
            developer("hsz", "Jakub Chrzanowski", "Jakub.Chrzanowski@jetbrains.com")
        }
    }

    publication {
        publicationName.set("intellij-platform")
        description.set("Kotlin Jupyter kernel integration for the IntelliJ Platform")
    }
}
