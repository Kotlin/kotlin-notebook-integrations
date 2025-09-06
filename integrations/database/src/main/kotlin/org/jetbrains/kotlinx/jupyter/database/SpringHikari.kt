@file:Suppress("UNCHECKED_CAST")

package org.jetbrains.kotlinx.jupyter.database

import com.zaxxer.hikari.HikariConfig
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.reflect.KClass

/**
 * TODO: ACHTUNG! This file is AI-generated. It may be dangerous to use.
 */
object SpringHikari {

    fun fromFile(path: String): HikariConfig =
        Files.newInputStream(Path.of(path)).use { fromInputStream(it) }

    fun fromInputStream(ins: InputStream): HikariConfig =
        Properties().apply { load(ins) }.let(::fromProperties)

    fun fromProperties(p: Properties): HikariConfig {
        val cfg = HikariConfig()

        // --- base datasource settings ---
        copyProperties(p, cfg) {
            copy("spring.datasource.jdbc-url", HikariConfig::setJdbcUrl)
            copy("spring.datasource.url", HikariConfig::setJdbcUrl)
            copy("spring.datasource.username", HikariConfig::setUsername)
            copy("spring.datasource.user", HikariConfig::setUsername)
            copy("spring.datasource.password", HikariConfig::setPassword)
            copy("spring.datasource.driver-class-name", HikariConfig::setDriverClassName)
            copy("spring.datasource.data-source-class-name", HikariConfig::setDataSourceClassName)
        }

        // --- hikari-options: spring.datasource.hikari.* ---
        // example: spring.datasource.hikari.maximum-pool-size=10
        val hikariPrefix = "spring.datasource.hikari."
        p.stringPropertyNames()
            .filter { it.startsWith(hikariPrefix) && !it.startsWith(hikariPrefix + "data-source-properties") }
            .forEach { fullKey ->
                val raw = p.getProperty(fullKey)
                val prop = toCamel(fullKey.removePrefix(hikariPrefix))
                setOnHikari(cfg, prop, raw)
            }

        // --- driver-specific properties (will go to HikariConfig.dataSourceProperties) ---
        // 1) spring.datasource.properties.<k>=<v>   (Spring Boot style)
        addDataSourceProps(p, "spring.datasource.properties.", cfg)

        // 2) spring.datasource.hikari.data-source-properties.<k>=<v>
        addDataSourceProps(p, "spring.datasource.hikari.data-source-properties.", cfg)

        return cfg
    }

    // ---------- helpers ----------

    private fun copyProperties(from: Properties, to: HikariConfig, copyAction: PropertiesCopyConfigurator.() -> Unit) {
        PropertiesCopyConfigurator(from, to).apply(copyAction)
    }

    private fun get(p: Properties, key: String): String? =
        p.getProperty(key)?.takeIf { it.isNotBlank() }?.let(::resolvePlaceholders)

    // very simple resolver for ${ENV} and ${prop} (+ default via colon: ${ENV:default})
    private fun resolvePlaceholders(s: String): String {
        var out = s
        val regex = Regex("""\$\{([^}]+)}""")
        while (true) {
            val m = regex.find(out) ?: break
            val token = m.groupValues[1]
            val (name, def) = token.split(':', limit = 2).let { it[0] to it.getOrNull(1) }
            val replacement = System.getenv(name)
                ?: System.getProperty(name)
                ?: def
                ?: ""
            out = out.replaceRange(m.range, replacement)
        }
        return out
    }

    private fun addDataSourceProps(p: Properties, prefix: String, cfg: HikariConfig) {
        p.stringPropertyNames()
            .filter { it.startsWith(prefix) }
            .forEach { k ->
                val propName = k.removePrefix(prefix)
                cfg.addDataSourceProperty(propName, resolvePlaceholders(p.getProperty(k)))
            }
    }

    private fun toCamel(s: String): String {
        // "maximum-pool-size" -> "maximumPoolSize", "connection-timeout" -> "connectionTimeout"
        return s.split('.', '-', '_')
            .filter { it.isNotEmpty() }
            .mapIndexed { i, part ->
                if (i == 0) part
                else part.replaceFirstChar { c -> c.uppercase() }
            }
            .joinToString("")
    }

    private fun setOnHikari(cfg: HikariConfig, prop: String, raw: String) {
        // Try to find a setter like setXxx(value) by property name (camelCase).
        val setterName = "set" + prop.replaceFirstChar { it.uppercase() }
        val methods = cfg::class.java.methods.filter { it.name == setterName && it.parameterCount == 1 }
        if (methods.isEmpty()) {
            // setter isn't found — assume the property is unknown; silently ignore
            return
        }
        val m = methods.first()
        val paramType = m.parameterTypes[0].kotlin
        val value = coerce(raw, paramType)
        m.invoke(cfg, value)
    }

    private fun coerce(raw: String, type: KClass<*>): Any = when (type) {
        java.lang.Boolean::class, Boolean::class -> raw.equals("true", true) || raw == "1" || raw.equals("yes", true)
        Integer::class, Int::class -> parseIntOrDuration(raw).toInt()
        java.lang.Long::class, Long::class -> parseIntOrDuration(raw)
        java.lang.Double::class, Double::class -> raw.toDouble()
        String::class -> raw
        else -> raw // fallback — let Hikari figure out what to do with it
    }

    // ms/s/m/h/d support (i.e., "30s", "5m"); otherwise — just long
    private fun parseIntOrDuration(s: String): Long {
        val trimmed = s.trim()
        val num = trimmed.takeWhile { it.isDigit() }
        val unit = trimmed.drop(num.length).lowercase()
        if (num.isEmpty()) return trimmed.toLong()
        val base = num.toLong()
        return when (unit) {
            "" -> base
            "ms" -> base
            "s"  -> base * 1_000
            "m"  -> base * 60_000
            "h"  -> base * 3_600_000
            "d"  -> base * 86_400_000
            else -> trimmed.toLong()
        }
    }

    private class PropertiesCopyConfigurator(
        private val p: Properties,
        private val cfg: HikariConfig
    ) {
        fun copy(
            propertyName: String,
            propSetter: HikariConfig.(String) -> Unit
        ) {
            get(p, propertyName)?.let { propSetter(cfg, it) }
        }
    }
}
