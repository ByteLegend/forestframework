package io.forestframework.testfixtures

import java.io.File

fun <T> withSystemPropertyConfigFile(tempDir: File, fileContent: String, function: () -> T): T {
    val originalConfig = System.getProperty("forest.config.file")
    val configFile = File(tempDir, "config.yml").apply { writeText(fileContent) }
    System.setProperty("forest.config.file", configFile.absolutePath)
    try {
        return function()
    } finally {
        if (originalConfig != null) {
            System.setProperty("forest.config.file", originalConfig)
        } else {
            System.clearProperty("forest.config.file")
        }
    }
}
