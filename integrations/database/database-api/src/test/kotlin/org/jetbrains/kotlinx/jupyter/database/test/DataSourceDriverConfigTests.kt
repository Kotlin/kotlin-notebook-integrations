package org.jetbrains.kotlinx.jupyter.database.test

import org.jetbrains.kotlinx.jupyter.database.DataSourceDriverConfig
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ClasspathDriverLoader
import org.jetbrains.kotlinx.jupyter.database.internal.drivers.ExternalDependencyDriverLoader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DataSourceDriverConfigTests {
    @BeforeEach
    fun setUp() {
        DataSourceDriverConfig.clearUserDrivers()
    }

    @AfterEach
    fun tearDown() {
        DataSourceDriverConfig.clearUserDrivers()
    }

    @Test
    fun defaultPlatformDriversPresent() {
        val names = DataSourceDriverConfig.driverLoaders.flatMap { it.names }
        assertTrue("postgres" in names)
        assertTrue("postgresql" in names)
        assertTrue("mysql" in names)
        assertTrue("h2" in names)
        assertTrue("sqlite" in names)
        assertTrue("duckdb" in names)
        assertTrue("mssql" in names)
        assertTrue("oracle" in names)
    }

    @Test
    fun addDriver_single_addsUserDriverFirstInList() {
        DataSourceDriverConfig.addDriver("mydb", "com.example:mydb:1.0.0")
        val first = DataSourceDriverConfig.driverLoaders.first()
        assertTrue(first is ExternalDependencyDriverLoader)
        assertTrue("mydb" in first.names)
    }

    @Test
    fun addDriver_multipleIdentifiers_allNamesPresent() {
        DataSourceDriverConfig.addDriver(listOf("mydb", "mydb2"), listOf("com.example:mydb:1.0.0"))
        val first = DataSourceDriverConfig.driverLoaders.first()
        assertTrue("mydb" in first.names)
        assertTrue("mydb2" in first.names)
    }

    @Test
    fun userDriversTakePrecedenceOverPlatformDrivers() {
        DataSourceDriverConfig.addDriver("postgres", "com.example:custom-pg:9.9.9")
        val loaders = DataSourceDriverConfig.driverLoaders
        val postgresLoaderIndex = loaders.indexOfFirst { "postgres" in it.names }
        val platformLoaderIndex = loaders.indexOfLast { "postgres" in it.names }
        assertTrue(postgresLoaderIndex < platformLoaderIndex, "User driver should come before platform driver")
    }

    @Test
    fun addDriver_multipleDrivers_mostRecentIsFirst() {
        DataSourceDriverConfig.addDriver("db1", "com.example:db1:1.0")
        DataSourceDriverConfig.addDriver("db2", "com.example:db2:1.0")
        val first = DataSourceDriverConfig.driverLoaders.first()
        assertTrue("db2" in first.names, "Most recently added user driver should be first")
    }

    @Test
    fun removeDriver_removesAddedDriver() {
        DataSourceDriverConfig.addDriver("mydb", "com.example:mydb:1.0.0")
        val removed = DataSourceDriverConfig.removeDriver("mydb")
        assertTrue(removed)
        assertFalse(DataSourceDriverConfig.driverLoaders.any { "mydb" in it.names })
    }

    @Test
    fun removeDriver_returnsFalseForNonexistentDriver() {
        val removed = DataSourceDriverConfig.removeDriver("nonexistent-db")
        assertFalse(removed)
    }

    @Test
    fun removeDriver_doesNotRemovePlatformDrivers() {
        val removed = DataSourceDriverConfig.removeDriver("postgres")
        assertFalse(removed, "removeDriver only removes user drivers, not platform drivers")
        assertTrue(DataSourceDriverConfig.driverLoaders.any { "postgres" in it.names })
    }

    @Test
    fun clearUserDrivers_removesAllUserDrivers() {
        DataSourceDriverConfig.addDriver("db1", "com.example:db1:1.0")
        DataSourceDriverConfig.addDriver("db2", "com.example:db2:1.0")
        DataSourceDriverConfig.clearUserDrivers()
        assertFalse(DataSourceDriverConfig.driverLoaders.any { "db1" in it.names })
        assertFalse(DataSourceDriverConfig.driverLoaders.any { "db2" in it.names })
    }

    @Test
    fun clearUserDrivers_keepsPlatformDrivers() {
        DataSourceDriverConfig.addDriver("db1", "com.example:db1:1.0")
        DataSourceDriverConfig.clearUserDrivers()
        val names = DataSourceDriverConfig.driverLoaders.flatMap { it.names }
        assertTrue("postgres" in names)
        assertTrue("h2" in names)
    }

    @Test
    fun useClasspathDriver_addsClasspathLoader() {
        DataSourceDriverConfig.useClasspathDriver("mydb")
        val classpathLoader = DataSourceDriverConfig.driverLoaders.filterIsInstance<ClasspathDriverLoader>().firstOrNull()
        assertNotNull(classpathLoader)
        assertTrue("mydb" in classpathLoader.names)
    }

    @Test
    fun useClasspathDriver_filtersBlankIdentifiers() {
        DataSourceDriverConfig.useClasspathDriver("mydb", " ", "")
        val classpathLoader = DataSourceDriverConfig.driverLoaders.filterIsInstance<ClasspathDriverLoader>().first()
        assertEquals(listOf("mydb"), classpathLoader.names)
    }

    @Test
    fun useClasspathDriver_appearsBeforePlatformDrivers() {
        DataSourceDriverConfig.useClasspathDriver("mydb")
        val first = DataSourceDriverConfig.driverLoaders.first()
        assertTrue(first is ClasspathDriverLoader)
    }
}
