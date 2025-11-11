plugins {
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

kotlin {
    jvmToolchain(
        libs.versions.jvmTarget
            .get()
            .toInt(),
    )
}

dependencies {
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback)

    // JDBC Drivers for all supported databases
    implementation(libs.mysqlDriver)
    implementation(libs.postgresDriver)
    implementation(libs.mssqlDriver)
    implementation(libs.oracleDriver)

    // Test containers for all supported databases
    implementation(libs.testContainers)
    implementation(libs.testContainers.postgres)
    implementation(libs.testContainers.mysql)
    implementation(libs.testContainers.mssqlserver)
    implementation(libs.testContainers.oracle.free)
}

application { mainClass = "org.jetbrains.kotlinx.jupyter.database.containers.MainKt" }

tasks.shadowJar {
    archiveClassifier.set("all")
    manifest { attributes["Main-Class"] = "org.jetbrains.kotlinx.jupyter.database.containers.MainKt" }
}

buildConfig {
    packageName("org.jetbrains.kotlinx.jupyter.database.gen")
    buildConfigField("Int", "TEST_SERVER_PORT", project.property("testServerPort").toString())
}
