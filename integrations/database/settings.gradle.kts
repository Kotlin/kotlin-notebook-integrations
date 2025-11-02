plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "kotlin-jupyter-database"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("database")
include("database-test-infrastructure")
include("database-integration-tests")
