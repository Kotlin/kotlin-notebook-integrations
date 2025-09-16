[![JetBrains official project](https://jb.gg/badges/official.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![Kotlin Alpha Stability](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)

# Database Connection Helpers

This repository contains a collection of database connection helpers for Kotlin Notebooks.

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
