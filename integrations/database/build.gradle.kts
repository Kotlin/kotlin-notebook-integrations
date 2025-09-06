import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.jupyter.api)
    alias(libs.plugins.publisher)
    alias(libs.plugins.ktlint)
}

val spaceUsername: String by properties
val spaceToken: String by properties

group = "org.jetbrains.kotlinx"
version =
    detectVersion().also { version ->
        Logging.getLogger(this::class.java).warn("Detected version: $version")
    }

allprojects {
    version = rootProject.version
}

private fun detectVersion(): String {
    val buildNumber = project.findProperty("build.number")
    if (buildNumber != null) {
        return buildNumber.toString()
    } else {
        val baseVersion = project.property("baseVersion").toString()
        val devAddition = project.property("devAddition").toString()
        return "$baseVersion-$devAddition-SNAPSHOT"
    }
}

kotlinJupyter {
    addApiDependency()
}

tasks.processJupyterApiResources {
    libraryProducers = listOf("org.jetbrains.kotlinx.jupyter.database.DatabaseJupyterIntegration")
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.hikari)
    api(libs.kotlin.jupyter.lib)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(libs.versions.jvmTarget.get().toInt())
}

kotlinPublications {
    defaultGroup.set(group.toString())
    fairDokkaJars.set(false)

    pom {
        inceptionYear.set("2025")
        licenses {
            apache2()
        }
        developers {
            developer("ileasile", "Ilya Muradyan", "Ilya.Muradyan@jetbrains.com")
        }
    }

    localRepositories {
        localMavenRepository(project.layout.buildDirectory.dir("maven"))
    }

    remoteRepositories {
        maven {
            name = "kotlin-ds-maven"
            url = uri("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
            credentials {
                logger.info("Space username: $spaceUsername")
                logger.info("Space token: ${spaceToken.replace(".".toRegex(), "*")}")
                username = spaceUsername
                password = spaceToken
            }
        }
    }

    publication {
        publicationName.set("kotlin-jupyter-database")
        description.set("Kotlin Jupyter kernel integration with DBMSs")
    }
}
