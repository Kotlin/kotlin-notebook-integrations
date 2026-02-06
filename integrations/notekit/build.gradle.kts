import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.publisher)
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-integrations")
        inceptionYear.set("2026")
        licenses {
            apache2()
        }
        developers {
            developer {
                id.set("kotlin-notebook-team")
                name.set("Kotlin Notebook Team")
                organization.set("JetBrains")
                organizationUrl.set("https://www.jetbrains.com")
            }
        }
    }
}
