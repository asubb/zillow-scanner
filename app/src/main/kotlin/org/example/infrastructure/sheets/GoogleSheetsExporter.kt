package org.example.infrastructure.sheets

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.application.PropertyExporter
import org.example.domain.ExportResult
import org.example.domain.Property
import org.example.infrastructure.config.AppConfig
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

class GoogleSheetsExporter(private val config: AppConfig) : PropertyExporter {
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
    
    private val sheetsService: Sheets by lazy {
        val credentials = GoogleCredentials.fromStream(FileInputStream(config.googleCredentialsFile))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))
        Sheets.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("Zillow Scanner")
            .build()
    }

    override fun export(properties: List<Property>, dryRun: Boolean): ExportResult {
        logger.debug { "Starting export of ${properties.size} properties (dryRun=$dryRun)" }
        if (properties.isEmpty()) return ExportResult(0, 0, 0)
        
        if (dryRun) {
            logger.info { "Dry run enabled, skipping write to Google Sheets" }
            return ExportResult(properties.size, 0, 0)
        }

        logger.debug { "Mapping properties to fields: ${config.exportFields}" }

        val values = properties.map { property ->
            config.exportFields.map { field ->
                when (field) {
                    "address" -> property.address
                    "price" -> {
                        // Clean price string for numeric conversion if possible
                        val numeric = property.price?.replace(Regex("[^0-9.]"), "")
                        numeric?.toDoubleOrNull() ?: (property.price ?: "")
                    }
                    "beds" -> property.beds ?: ""
                    "baths" -> property.baths ?: ""
                    "sqft" -> property.sqft ?: ""
                    "url" -> property.url ?: ""
                    "lastScanned" -> {
                        // Convert Instant to Excel-style serial number for Google Sheets
                        // Days since Dec 30, 1899
                        val secondsInDay = 86400.0
                        val excelEpochSeconds = -2209161600L // 1899-12-30T00:00:00Z
                        val diffSeconds = property.lastScanned.epochSecond - excelEpochSeconds
                        diffSeconds / secondsInDay + (property.lastScanned.nano / (secondsInDay * 1_000_000_000.0))
                    }
                    else -> ""
                }
            }
        }

        val listValues: List<List<Any>> = values.map { it.map { item -> item as Any } }

        // Detailed logging of values structure
        logger.debug { "Values structure: size=${listValues.size}, first row size=${if (listValues.isNotEmpty()) listValues[0].size else 0}" }
        if (listValues.isNotEmpty()) {
            logger.debug { "Sample row values (row 0): ${listValues[0]}" }
        }

        val body = ValueRange().setValues(listValues)
        val range = if (config.sheetName.isNotBlank()) "${config.sheetName}!A1" else "A1"
        
        return try {
            val spreadsheet = sheetsService.spreadsheets().get(config.spreadsheetId).execute()
            val sheet = spreadsheet.sheets.find { it.properties.title == config.sheetName } 
                ?: spreadsheet.sheets.first()
            val sheetId = sheet.properties.sheetId

            val currentValues = sheetsService.spreadsheets().values()
                .get(config.spreadsheetId, range)
                .execute()
                .getValues()

            val isNewSheet = currentValues == null || currentValues.isEmpty()
            
            if (isNewSheet) {
                logger.info { "Sheet appears to be empty. Formatting and performing initial update at $range" }
                applyFormatting(sheetId)
                
                val headerRow = config.exportFields.map { it.replaceFirstChar { c -> c.uppercase() } }
                val valuesWithHeaders = mutableListOf<List<Any>>(headerRow)
                valuesWithHeaders.addAll(listValues)
                val bodyWithHeaders = ValueRange().setValues(valuesWithHeaders)
                
                sheetsService.spreadsheets().values()
                    .update(config.spreadsheetId, range, bodyWithHeaders)
                    .setValueInputOption("USER_ENTERED")
                    .execute()
            } else {
                logger.info { "Sheet has data. Appending after existing rows." }
                sheetsService.spreadsheets().values()
                    .append(config.spreadsheetId, range, body)
                    .setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS")
                    .execute()
            }
            
            ExportResult(properties.size, 0, 0)
        } catch (e: Exception) {
            logger.error(e) { "ERROR during Google Sheets export: ${e.message}" }
            ExportResult(0, 0, properties.size, listOf(e.message ?: "Unknown error"))
        }
    }

    private fun applyFormatting(sheetId: Int) {
        val requests = mutableListOf<Request>()

        // Header Formatting: Bold, Background, Center alignment
        requests.add(Request().setRepeatCell(RepeatCellRequest().apply {
            range = GridRange().setSheetId(sheetId).setStartRowIndex(0).setEndRowIndex(1)
            cell = CellData().setUserEnteredFormat(CellFormat().apply {
                textFormat = TextFormat().setBold(true)
                backgroundColor = Color().setRed(0.9f).setGreen(0.9f).setBlue(0.9f) // Light Grey
                horizontalAlignment = "CENTER"
            })
            fields = "userEnteredFormat(textFormat,backgroundColor,horizontalAlignment)"
        }))

        // Column Specific Formatting
        config.exportFields.forEachIndexed { index, field ->
            val format = when (field) {
                "price" -> CellFormat().setNumberFormat(NumberFormat().setType("CURRENCY").setPattern("$#,##0"))
                "lastScanned" -> CellFormat().setNumberFormat(NumberFormat().setType("DATE_TIME").setPattern("yyyy-mm-dd HH:mm:ss"))
                "beds", "baths", "sqft" -> CellFormat().setNumberFormat(NumberFormat().setType("NUMBER").setPattern("#,##0.##"))
                else -> null
            }

            if (format != null) {
                requests.add(Request().setRepeatCell(RepeatCellRequest().apply {
                    range = GridRange().setSheetId(sheetId).setStartColumnIndex(index).setEndColumnIndex(index + 1).setStartRowIndex(1)
                    cell = CellData().setUserEnteredFormat(format)
                    fields = "userEnteredFormat.numberFormat"
                }))
            }
        }

        // Freeze Header Row
        requests.add(Request().setUpdateSheetProperties(UpdateSheetPropertiesRequest().apply {
            properties = SheetProperties().setSheetId(sheetId).setGridProperties(GridProperties().setFrozenRowCount(1))
            fields = "gridProperties.frozenRowCount"
        }))

        // Set Basic Filter
        requests.add(Request().setSetBasicFilter(SetBasicFilterRequest().apply {
            filter = BasicFilter().setRange(GridRange().setSheetId(sheetId).setStartRowIndex(0))
        }))

        val batchRequest = BatchUpdateSpreadsheetRequest().setRequests(requests)
        sheetsService.spreadsheets().batchUpdate(config.spreadsheetId, batchRequest).execute()
    }
}
