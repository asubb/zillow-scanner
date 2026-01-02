package org.example.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.serialization.json.Json
import org.example.domain.BrowserConnectionException
import org.example.domain.ScanResult
import org.example.infrastructure.browser.PlaywrightScanner

class ScanCommand : CliktCommand(name = "scan") {
    override fun help(context: Context): String = "Scans the current Zillow tab and displays results"
    private val json by option("--json", help = "Output in JSON format instead of table").flag()
    private val allPages by option("--all-pages", help = "Scan all pagination pages").flag()
    private val port by option("--port", help = "Chrome debug port").convert { it.toInt() }.default(9222)

    override fun run() {
        val scanner = PlaywrightScanner(port)
        
        try {
            scanner.connect()
        } catch (e: BrowserConnectionException) {
            echo(e.message, err = true)
            throw ProgramResult(1)
        }

        val page = scanner.findZillowPage()
        if (page == null) {
            echo("No Zillow tab found", err = true)
            scanner.disconnect()
            throw ProgramResult(2)
        }

        val result = scanner.scan(page, allPages)
        
        if (json) {
            val jsonString = Json { prettyPrint = true }.encodeToString(ScanResult.serializer(), result)
            echo(jsonString)
        } else {
            echo("Scanned: ${result.sourceUrl}")
            echo("Found ${result.extractedCount} properties (${result.errorCount} errors)")
            echo("".padEnd(80, '-'))
            echo("${"Address".padEnd(40)} | ${"Price".padEnd(15)} | ${"Bds/Ba/Sqft".padEnd(15)} | URL")
            echo("".padEnd(120, '-'))
            result.properties.forEach {
                val details = "${it.beds ?: "?"}/${it.baths ?: "?"}/${it.sqft ?: "?"}"
                echo("${it.address.take(40).padEnd(40)} | ${it.price?.padEnd(15) ?: "N/A".padEnd(15)} | ${details.padEnd(15)} | ${it.url ?: "N/A"}")
            }
        }

        scanner.disconnect()
    }
}

