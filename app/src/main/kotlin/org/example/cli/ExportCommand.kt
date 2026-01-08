package org.example.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.example.infrastructure.browser.PlaywrightScanner
import org.example.infrastructure.config.AppConfig
import org.example.infrastructure.sheets.GoogleSheetsExporter
import java.io.File
import kotlin.system.exitProcess

class ExportCommand : CliktCommand(name = "export") {
    override fun help(context: Context): String = "Scans Zillow and exports results to Google Sheets"
    
    private val defaultConfigPath = File(System.getProperty("user.home"), ".zillow-scanner/config.yaml").absolutePath
    private val configPath by option("--config", help = "Path to config YAML file").default(defaultConfigPath)
    private val dryRun by option("--dry-run", help = "Perform a scan but do not write to Google Sheets").flag()
    private val allPages by option("--all-pages", help = "Scan all pagination pages").flag()

    override fun run() {
        if (!File(configPath).exists()) {
            echo("Config file not found: $configPath", err = true)
            throw ProgramResult(1)
        }

        val config = AppConfig.load(configPath)
        val scanner = PlaywrightScanner(config.chromeDebugPort)
        val exporter = GoogleSheetsExporter(config)

        echo("Connecting to Chrome on port ${config.chromeDebugPort}...")
        try {
            scanner.connect()
        } catch (e: Exception) {
            echo("Failed to connect to Chrome: ${e.message}", err = true)
            throw ProgramResult(1)
        }

        val page = scanner.findZillowPage()
        if (page == null) {
            echo("No Zillow tab found", err = true)
            scanner.disconnect()
            throw ProgramResult(1)
        }

        echo("Scanning properties...")
        val scanResult = scanner.scan(page, allPages)
        echo("Found ${scanResult.extractedCount} properties.")

        if (scanResult.extractedCount > 0) {
            echo("Exporting to Google Sheets (Spreadsheet ID: ${config.spreadsheetId})...")
            val exportResult = exporter.export(scanResult.properties, dryRun)
            
            if (dryRun) {
                echo("[DRY RUN] Would have processed ${scanResult.extractedCount} properties.")
            } else {
                if (exportResult.failedCount == 0) {
                    echo("Successfully exported properties.")
                    echo("  Inserted: ${exportResult.insertedCount}")
                    echo("  Updated:  ${exportResult.updatedCount}")
                } else {
                    echo("Export partially failed.", err = true)
                    echo("  Inserted: ${exportResult.insertedCount}", err = true)
                    echo("  Updated:  ${exportResult.updatedCount}", err = true)
                    echo("  Failed:   ${exportResult.failedCount}", err = true)
                    exportResult.errors.forEach { echo("Error: $it", err = true) }
                }
            }
        }

        scanner.disconnect()
    }
}
