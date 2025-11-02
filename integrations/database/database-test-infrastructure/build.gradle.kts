plugins {
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
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
    val ktor = libs.versions.ktor.get()
    implementation("io.ktor:ktor-server-cio:$ktor")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor")
    implementation(libs.logback)

    // JDBC Drivers for all supported databases
    implementation("com.mysql:mysql-connector-j:9.4.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.microsoft.sqlserver:mssql-jdbc:13.2.0.jre11")
    implementation("com.oracle.database.jdbc:ojdbc11:23.9.0.25.07")

    // Test containers for all supported databases
    implementation(libs.testContainers)
    val testContainerVersion =
        libs.versions.testContainers.modules
            .get()
    implementation("org.testcontainers:postgresql:$testContainerVersion")
    implementation("org.testcontainers:mysql:$testContainerVersion")
    implementation("org.testcontainers:mssqlserver:$testContainerVersion")
    implementation("org.testcontainers:oracle-free:$testContainerVersion")
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
