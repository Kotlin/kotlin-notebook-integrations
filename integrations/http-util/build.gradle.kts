import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.publisher)
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-libraries")
        inceptionYear = "2024"
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

