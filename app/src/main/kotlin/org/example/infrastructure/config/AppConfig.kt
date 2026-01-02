package org.example.infrastructure.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.io.File

@Serializable
data class AppConfig(
    val googleCredentialsFile: String,
    val spreadsheetId: String,
    val sheetName: String = "Properties",
    val exportFields: List<String> = emptyList(),
    val chromeDebugPort: Int = 9222
) {
    companion object {
        fun load(path: String): AppConfig {
            val configText = File(path).readText()
            return Yaml.default.decodeFromString(configText)
        }
    }
}
