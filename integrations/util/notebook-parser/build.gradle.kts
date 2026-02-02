import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.versions)
    alias(libs.plugins.kover)
    alias(libs.plugins.publisher)
}

fun detectVersion(): String {
    val buildNumber = rootProject.findProperty("build.number") as? String
    val defaultVersion = property("version").toString()

    return if (buildNumber != null) {
        if (hasProperty("build.number.detection")) {
            "$defaultVersion-dev-$buildNumber"
        } else {
            buildNumber
        }
    } else if (hasProperty("release")) {
        defaultVersion
    } else {
        "$defaultVersion-dev"
    }
}

val detectVersionForTC: Task by tasks.creating {
    doLast {
        println("##teamcity[buildNumber '$version']")
    }
}

group = "org.jetbrains.kotlinx"
version = detectVersion()

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

kotlinPublications {
    defaultArtifactIdPrefix.set("")
    fairDokkaJars.set(false)

    fun <T> Project.typedProperty(name: String): T {
        @Suppress("UNCHECKED_CAST")
        return findProperty(name) as T
    }

    sonatypeSettings(
        typedProperty("kds.sonatype.user"),
        typedProperty("kds.sonatype.password"),
        "jupyter-notebooks-parser project, v. ${project.version}"
    )

    signingCredentials(
        typedProperty("kds.sign.key.id"),
        typedProperty("kds.sign.key.private"),
        typedProperty("kds.sign.key.passphrase")
    )

    pom {
        githubRepo("ileasile", "jupyter-notebooks-parser")
        inceptionYear.set("2021")
        licenses {
            apache2()
        }
        developers {
            developer("ileasile", "Ilya Muradyan", "ilya.muradyan@jetbrains.com")
        }
    }

    localRepositories {
        // Default location for the local repository is build/artifacts/maven/
        defaultLocalMavenRepository()
    }

    publication {
        description.set("Jupyter Notebooks parser and Kotlin utilities for them")
    }
}
