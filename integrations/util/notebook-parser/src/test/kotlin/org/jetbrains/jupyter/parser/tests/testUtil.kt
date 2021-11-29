package org.jetbrains.jupyter.parser.tests

import java.io.File

val testDataDirectory = File("src/test/testData")

inline fun <reified T> testDataDir() = testDataDirectory.resolve(T::class.simpleName!!)
