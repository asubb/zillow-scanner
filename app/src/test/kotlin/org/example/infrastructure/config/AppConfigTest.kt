package org.example.infrastructure.config

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppConfigTest {
    private val testConfigFile = File("test-config.yaml")

    @BeforeTest
    fun setup() {
        testConfigFile.writeText("""
            googleCredentialsFile: "credentials.json"
            spreadsheetId: "1abc123"
            sheetName: "ZillowProperties"
            exportFields:
              - "address"
              - "price"
            chromeDebugPort: 9222
        """.trimIndent())
    }

    @AfterTest
    fun cleanup() {
        if (testConfigFile.exists()) {
            testConfigFile.delete()
        }
    }

    @Test
    fun `should load config from yaml file`() {
        val config = AppConfig.load(testConfigFile.path)
        
        assertEquals("credentials.json", config.googleCredentialsFile)
        assertEquals("1abc123", config.spreadsheetId)
        assertEquals("ZillowProperties", config.sheetName)
        assertEquals(listOf("address", "price"), config.exportFields)
        assertEquals(9222, config.chromeDebugPort)
    }

    @Test
    fun `should load config with default values`() {
        testConfigFile.writeText("""
            googleCredentialsFile: "credentials.json"
            spreadsheetId: "1abc123"
        """.trimIndent())
        
        val config = AppConfig.load(testConfigFile.path)
        
        assertEquals("Properties", config.sheetName)
        assertEquals(9222, config.chromeDebugPort)
        assertEquals(listOf("address", "price", "beds", "baths", "sqft", "url", "lastScanned"), config.exportFields)
    }
}
