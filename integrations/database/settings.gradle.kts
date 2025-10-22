plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "kotlin-jupyter-database"

include("library")
include("test-infrastructure")
include("library-integration-tests")