import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.publisher)
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-integrations")
        inceptionYear.set("2025")
        licenses {
            apache2()
        }
        developers {
            developer {
                id.set("kotlin-jupyter-team")
                name.set("Kotlin Jupyter Team")
                organization.set("JetBrains")
                organizationUrl.set("https://www.jetbrains.com")
            }
        }
    }
}

subprojects {
    if (name == "widgets-api" || name == "widgets-jupyter") {
        tasks.configureEach {
            if (name.contains("KotlinCompile") ||
                name.contains("compileKotlin") ||
                name.contains("sourcesJar") ||
                name.contains("ktlint", ignoreCase = true)
            ) {
                dependsOn(":integrations:widgets:widgets-generator:generateWidgets")
            }
        }
    }
}
