package org.jetbrains.kotlinx.jupyter.database.internal

import com.zaxxer.hikari.HikariConfig
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.reflect.KClass

/**
 * Creates a [com.zaxxer.hikari.HikariConfig] from a Spring application.properties file. Both
 * properties (.properties) and yaml formats (.yml, .yaml) are supported.
 */
internal object SpringHikari {

    fun fromFile(path: String): HikariConfig = fromFile(Path.of(path))

    fun fromFile(path: Path): HikariConfig {
        if (!path.exists()) throw IllegalArgumentException("${path.absolutePathString()} does not exist")
        require(path.exists()) { "File ${path.absolutePathString()} does not exist"}
        val fileType = path.absolutePathString().substringAfterLast(".").lowercase()
        return when (fileType) {
            "properties" -> {
                val props = Files.newInputStream(path).use { inputStream ->
                    Properties().apply {
                        load(inputStream)
                    }
                }
                fromProperties(props)
            }
            "yml",
            "yaml" -> {
                val settings = LoadSettings.builder().build()
                val load = Load(settings)
                @Suppress("UNCHECKED_CAST")
                val yaml = Files.newBufferedReader(path).use { r ->
                    val result = load.loadFromReader(r) as? Map<String, Any?>
                        ?: throw IllegalArgumentException("YAML file is not valid: ${path.absolutePathString()}")
                    result
                }
                val props = Properties()
                flattenToProps(yaml, props)
                fromProperties(props)
            }
            else -> throw IllegalArgumentException("Unsupported file type ($fileType): ${path.absolutePathString()}")
        }
    }

    /**
     * Flatten a nested map (from a Yaml file) to a [Properties] object.
     */
    private fun flattenToProps(src: Map<String, Any?>, out: Properties, prefix: String = "") {
        val pre = if (prefix.isEmpty()) "" else "$prefix."
        src.forEach { (key: String, value: Any?) ->
            @Suppress("UNCHECKED_CAST")
            when (value) {
                is Map<*, *> -> flattenToProps(value as Map<String, Any?>, out, pre + key)
                is Iterable<*> -> out.setProperty(pre + key, value.joinToString(",") { it.toString() })
                null -> { /* Skip nulls */ }
                else -> out.setProperty(pre + key, value.toString())
            }
        }
    }

    private fun fromProperties(p: Properties): HikariConfig {
        val cfg = HikariConfig()

        // Set base data source settings manually
        // For now, we only support a subset of properties. We need user feedback to
        // know if more is needed.
        copyProperties(p, cfg) {
            copy("spring.datasource.jdbc-url", HikariConfig::setJdbcUrl)
            copy("spring.datasource.url", HikariConfig::setJdbcUrl)
            copy("spring.datasource.username", HikariConfig::setUsername)
            copy("spring.datasource.user", HikariConfig::setUsername)
            copy("spring.datasource.password", HikariConfig::setPassword)
            copy("spring.datasource.driver-class-name", HikariConfig::setDriverClassName)
            copy("spring.datasource.data-source-class-name", HikariConfig::setDataSourceClassName)
        }

        // Set all base Hikari properties.
        val hikariPrefix = "spring.datasource.hikari."
        p.stringPropertyNames()
            .filter { it.startsWith(hikariPrefix) && !it.startsWith(hikariPrefix + "data-source-properties") }
            .forEach { fullKey ->
                val raw = p.getProperty(fullKey)
                val prop = toCamel(fullKey.removePrefix(hikariPrefix))
                setOnHikari(cfg, prop, raw)
            }

        // Set all driver-specific properties (those that belong to HikariConfig.dataSourceProperties)
        // We have two cases:
        // 1) spring.datasource.properties.<k>=<v> (Spring Boot style)
        // 2) spring.datasource.hikari.data-source-properties.<k>=<v>
        // Unknown properties will be silently ignored.
        addDataSourceProperty(p, "spring.datasource.properties.", cfg)
        addDataSourceProperty(p, "spring.datasource.hikari.data-source-properties.", cfg)

        return cfg
    }

    private fun copyProperties(
        from: Properties,
        to: HikariConfig,
        action: PropertiesCopyConfigurator.() -> Unit
    ) {
        PropertiesCopyConfigurator(from, to).apply(action)
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

    private fun addDataSourceProperty(p: Properties, prefix: String, cfg: HikariConfig) {
        p.stringPropertyNames()
            .filter { it.startsWith(prefix) }
            .forEach { k ->
                val propName = k.removePrefix(prefix)
                cfg.addDataSourceProperty(propName, resolvePlaceholders(p.getProperty(k)))
            }
    }

    // Convert naming schemes found in .properties files to camelCase naming
    // Only Ascii characters are supported.
    // E.g. "maximum-pool-size" -> "maximumPoolSize"
    private fun toCamel(s: String): String {
        return s.split('.', '-', '_')
            .filter { it.isNotEmpty() }
            .mapIndexed { i, part ->
                if (i == 0) part.replaceFirstChar { c -> c.lowercase() }
                else part.replaceFirstChar { c -> c.uppercase() }
            }
            .joinToString("")
    }

    private fun setOnHikari(cfg: HikariConfig, prop: String, raw: String) {
        // Try to find a setter like setXxx(value) by property name (camelCase).
        val setterName = "set" + prop.replaceFirstChar { it.uppercase() }
        val methods = cfg::class.java.methods.filter { it.name == setterName && it.parameterCount == 1 }
        if (methods.isEmpty()) {
            // Setter isn't found.
            // Assume the property is unknown and silently ignore it
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