plugins {
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jupyter.api)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

dependencies {
    // We must not depend on `project(":library")` here as prevents us from testing that
    // drivers can be loaded correctly. Instead we depend on the Notebook Kernel for this
    // as it will use `@file:Depends("..")` to load it.
    testImplementation(libs.kotlin.jupyter.lib)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.contentNegotiation)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.serialization.json)
    testImplementation(libs.test.kotlintest.assertions)
}

kotlin {
    jvmToolchain(
        libs.versions.jvmTarget
            .get()
            .toInt(),
    )
}

buildConfig {
    packageName("org.jetbrains.kotlinx.jupyter.database.gen")
    buildConfigField("Int", "TEST_SERVER_PORT", project.property("testServerPort").toString())
    buildConfigField("String", "LIBRARY_VERSION", "\"${project.version}\"")
}

tasks.test {
    useJUnitPlatform()
}

/**
 * A test server is started in the background when running tests. This server is used by the test infrastructure
 * to spinup database containers as needed.
 *
 * All containers are killed when the test suite is finished.
 */
evaluationDependsOn(projects.databaseTestInfrastructure.path)
val testServerPort = project.findProperty("testServerPort") ?: error("Missing Gradle property: `testServerPort`")
val libraryPublishTask = project(projects.database.path).tasks.named<DefaultTask>("publishToMavenLocal")
val testServerJarTask = project(projects.databaseTestInfrastructure.path).tasks.named<Jar>("shadowJar")
val testServerJarFile: Provider<RegularFile> = testServerJarTask.flatMap { it.archiveFile }
val startTestServer by tasks.registering(Exec::class) {
    dependsOn(libraryPublishTask, testServerJarTask)
    isIgnoreExitValue = true
    doFirst {
        val cmd =
            """
            nohup java -jar '${testServerJarFile.get().asFile.absolutePath}' --port $testServerPort >/dev/null 2>&1 &
            """.trimIndent()
        commandLine("bash", "-c", cmd)
    }
    inputs.file(testServerJarFile)
}

val stopTestServer by tasks.registering(Exec::class) {
    commandLine("bash", "-lc", "kill -9 $(lsof -ti:$testServerPort) || true")
}

tasks.withType<Test>().configureEach {
    dependsOn(startTestServer)
    finalizedBy(stopTestServer)
}
