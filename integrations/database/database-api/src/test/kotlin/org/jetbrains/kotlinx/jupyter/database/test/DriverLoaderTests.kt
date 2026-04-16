package org.jetbrains.kotlinx.jupyter.database.test

import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ClasspathDriverLoader
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ExternalDependencyDriverLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DriverLoaderTests {
    // ── ClasspathDriverLoader ──────────────────────────────────────────────

    @Test
    fun classpathLoader_shouldLoadDriver_alwaysReturnsFalse() {
        val loader = ClasspathDriverLoader(listOf("mydb"))
        assertFalse(loader.shouldLoadDriver("jdbc:mydb://localhost/db"))
        assertFalse(loader.shouldLoadDriver("jdbc:other://localhost/db"))
        assertFalse(loader.shouldLoadDriver(""))
    }

    @Test
    fun classpathLoader_names_returnsConfiguredNames() {
        val loader = ClasspathDriverLoader(listOf("mydb", "mydb2"))
        assertEquals(listOf("mydb", "mydb2"), loader.names)
    }

    @Test
    fun classpathLoader_emptyNames() {
        val loader = ClasspathDriverLoader(emptyList())
        assertEquals(emptyList(), loader.names)
        assertFalse(loader.shouldLoadDriver("jdbc:anything://host/db"))
    }

    // ── ExternalDependencyDriverLoader ─────────────────────────────────────

    @Test
    fun externalLoader_shouldLoadDriver_trueForMatchingUrl() {
        val loader = ExternalDependencyDriverLoader(
            listOf("postgres", "postgresql"),
            listOf("org.postgresql:postgresql:42.0.0"),
        )
        assertTrue(loader.shouldLoadDriver("jdbc:postgres://localhost/mydb"))
        assertTrue(loader.shouldLoadDriver("jdbc:postgresql://localhost/mydb"))
    }

    @Test
    fun externalLoader_shouldLoadDriver_falseForNonMatchingUrl() {
        val loader = ExternalDependencyDriverLoader(
            listOf("postgres"),
            listOf("org.postgresql:postgresql:42.0.0"),
        )
        assertFalse(loader.shouldLoadDriver("jdbc:mysql://localhost/mydb"))
        assertFalse(loader.shouldLoadDriver("jdbc:h2:mem:test"))
        assertFalse(loader.shouldLoadDriver("jdbc:sqlite::memory:"))
    }

    @Test
    fun externalLoader_shouldLoadDriver_requiresJdbcPrefix() {
        val loader = ExternalDependencyDriverLoader(
            listOf("postgres"),
            listOf("org.postgresql:postgresql:42.0.0"),
        )
        // raw "postgres://..." without the jdbc: prefix should not match
        assertFalse(loader.shouldLoadDriver("postgres://localhost/mydb"))
    }

    @Test
    fun externalLoader_names_returnsConfiguredNames() {
        val loader = ExternalDependencyDriverLoader(
            listOf("mssql", "sqlserver"),
            listOf("com.microsoft.sqlserver:mssql-jdbc:13.0.0"),
        )
        assertEquals(listOf("mssql", "sqlserver"), loader.names)
    }

    @Test
    fun externalLoader_shouldLoadDriver_falseForEmptyUrl() {
        val loader = ExternalDependencyDriverLoader(
            listOf("postgres"),
            listOf("org.postgresql:postgresql:42.0.0"),
        )
        assertFalse(loader.shouldLoadDriver(""))
    }
}
