import org.jetbrains.kotlinx.publisher.apache2
import org.jetbrains.kotlinx.publisher.developer
import org.jetbrains.kotlinx.publisher.githubRepo

plugins {
    alias(libs.plugins.publisher)
}

subprojects {
    // Allow skipping DB integration tests via -PskipDbIntegrationTests=true
    val skipDbIntegrationTests =
        providers
            .gradleProperty("skipDbIntegrationTests")
            .orNull
            ?.toBoolean() == true

    if (skipDbIntegrationTests) {
        tasks.withType<Test> {

            // DB integration tests are placed under this package
            exclude("org/jetbrains/kotlinx/jupyter/database/test/integration/**")
            failOnNoDiscoveredTests = false
        }
    }
}

kotlinPublications {
    pom {
        githubRepo("Kotlin", "kotlin-notebook-libraries")
        inceptionYear.set("2025")
        licenses {
            apache2()
        }
        developers {
            developer("ileasile", "Ilya Muradyan", "Ilya.Muradyan@jetbrains.com")
            developer("cmelchior", "Christian Melchior", "Christian.Melchior@jetbrains.com")
        }
    }
}
