plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.publisher)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktlint)
}

version =
    detectVersion().also { version ->
        Logging.getLogger(this::class.java).warn("Detected version: $version")
    }

allprojects {
    group = "org.jetbrains.kotlinx"
    version = rootProject.version

    plugins.apply("org.jlleitschuh.gradle.ktlint")
    ktlint {
        filter {
            exclude { entry ->
                entry.file.toString().contains("generated")
            }
        }
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        maxHeapSize = "4G"
        outputs.upToDateWhen { false }
    }
}

val spaceUsername: String by properties
val spaceToken: String by properties

kotlinPublications {
    defaultGroup.set(group.toString())
    defaultArtifactIdPrefix.set("kotlin-jupyter-")
    fairDokkaJars.set(false)

    // Maven Central publishing properties
    val sonatypeUsername: String = System.getenv("SONATYPE_USER") ?: ""
    val sonatypePassword: String = System.getenv("SONATYPE_PASSWORD") ?: ""
    val signingKey: String? = System.getenv("SIGN_KEY_ID")
    val signingPrivateKey: String? = System.getenv("SIGN_KEY_PRIVATE")
    val signingKeyPassphrase: String? = System.getenv("SIGN_KEY_PASSPHRASE")

    sonatypeSettings(
        sonatypeUsername,
        sonatypePassword,
        "kotlin-notebook-integrations project, v. ${project.version}",
    )

    signingCredentials(
        signingKey,
        signingPrivateKey,
        signingKeyPassphrase,
    )

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

tasks.wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
    distributionUrl = "https://cache-redirector.jetbrains.com/services.gradle.org/distributions/gradle-$gradleVersion-all.zip"
}
