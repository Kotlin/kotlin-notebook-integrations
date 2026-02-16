[![JetBrains official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin Alpha Stability](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![Maven Central](https://img.shields.io/maven-central/v/org.jetbrains.kotlinx/kotlin-jupyter-database?color=blue&label=Maven%20Central)](https://central.sonatype.com/artifact/org.jetbrains.kotlinx/kotlin-jupyter-database)

# Database Connection Helpers

This repository contains a collection of database connection helpers for Kotlin 
Notebooks.

## Requirements

* These APIs require JVM 17 or higher.

## Usage

Use these API's through the `%use database` magic command in a Kotlin Notebook.

```
%use database

// Create a java.sql.DataSource for use in other libraries or to execute raw sql.

// Simple data sources
val dataSource1 = createDataSrc(
    jdbcUrl = "jdbc:postgresql://localhost:5432/postgres",
    username = "test",
    password = "test"
)

// Configure a data source using HikariConfig 
val dataSource2 = createDataSrc {
    jdbcUrl = "jdbc:postgresql://localhost:5432/postgres"
    username = "test"
    password = "test"
    readOnly = true
}

// Configure a data source using a Spring application file (.properties or .yml/.yaml)
val path = Path.of("/absolute/path/to/application.properties")
val dataSource3 = createDataSrcFromSpring(path)
```

## Module structure

This project consists of the following modules:

- `database-api`: Contains the core database connection helpers as well as basic unit 
   tests.
- `database-integration-tests`: Contains integration tests against all supported 
   databases and ensure we can load JDBC drivers correctly.
- `database-test-infrastructure`: A HTTP test server that allows integration tests to 
   spin up database containers on demand. It is built and run from the 
   integration test module.

Running all tests and checks are done by running this command the root directory:

```shell
./gradlew check
```

Note, it requires that `docker` is available on the PATH of the system.
