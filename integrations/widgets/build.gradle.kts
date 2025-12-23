import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.publisher)
}

subprojects {
    ktlint {
        filter {
            exclude("**/src/generated/**")
        }
    }
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-integrations")
        inceptionYear = "2025"
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
