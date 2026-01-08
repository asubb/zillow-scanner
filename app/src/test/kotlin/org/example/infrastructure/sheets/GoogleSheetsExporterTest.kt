package org.example.infrastructure.sheets

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.domain.Property
import org.example.infrastructure.config.AppConfig
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class GoogleSheetsExporterTest {
    private val config = AppConfig(
        spreadsheetId = "test-spreadsheet-id",
        sheetName = "Sheet1",
        googleCredentialsFile = "test-creds.json",
        exportFields = listOf("address", "price", "beds", "baths", "sqft", "url", "lastScanned")
    )

    private val mockSheets: Sheets = mockk()
    private val mockSpreadsheets: Sheets.Spreadsheets = mockk()
    private val mockValues: Sheets.Spreadsheets.Values = mockk()

    init {
        every { mockSheets.spreadsheets() } returns mockSpreadsheets
        every { mockSpreadsheets.values() } returns mockValues
    }

    @Test
    fun `export should update existing row when address matches`() {
        // Arrange
        val exporter = GoogleSheetsExporter(config, mockSheets)
        val property = Property(
            address = "123 Main St",
            price = "$500,000",
            beds = 3,
            baths = 2.0,
            sqft = 1500,
            url = "http://zillow.com/123",
            lastScanned = Instant.parse("2023-01-01T12:00:00Z")
        )

        val existingHeader = listOf("Address", "Price", "Beds", "Baths", "Sqft", "Url", "LastScanned")
        val existingRow = listOf("123 Main St", "$450,000", "3", "2.0", "1500", "http://zillow.com/123", "44927.5") // 2023-01-01
        
        val getResponse = ValueRange().setValues(listOf(existingHeader, existingRow))
        
        every { mockValues.get("test-spreadsheet-id", "Sheet1").execute() } returns getResponse
        
        val spreadsheet = Spreadsheet().setSheets(listOf(
            Sheet().setProperties(SheetProperties().setTitle("Sheet1").setSheetId(0))
        ))
        every { mockSpreadsheets.get("test-spreadsheet-id").execute() } returns spreadsheet

        // Mock the batch update call
        val batchUpdateResponse = BatchUpdateValuesResponse()
        every { 
            mockValues.batchUpdate("test-spreadsheet-id", any())
                .execute() 
        } returns batchUpdateResponse

        // Act
        val result = exporter.export(listOf(property), dryRun = false)

        // Assert
        assertEquals(0, result.insertedCount)
        assertEquals(1, result.updatedCount)
        assertEquals(0, result.failedCount)

        // Verify that batchUpdate was called
        verify {
            mockValues.batchUpdate("test-spreadsheet-id", withArg {
                assertEquals("USER_ENTERED", it.valueInputOption)
                assertEquals(1, it.data.size)
                assertEquals("Sheet1!A2", it.data[0].range)
            })
        }
        
        verify {
            mockValues.get("test-spreadsheet-id", "Sheet1")
        }
    }

    @Test
    fun `export should insert new row when address does not match`() {
        // Arrange
        val exporter = GoogleSheetsExporter(config, mockSheets)
        val property = Property(
            address = "456 Oak St",
            price = "$600,000"
        )

        val existingHeader = listOf("Address", "Price", "Beds", "Baths", "Sqft", "Url", "LastScanned")
        val existingRow = listOf("123 Main St", "$450,000")
        
        val getResponse = ValueRange().setValues(listOf(existingHeader, existingRow))
        
        every { mockValues.get("test-spreadsheet-id", "Sheet1").execute() } returns getResponse
        
        val spreadsheet = Spreadsheet().setSheets(listOf(
            Sheet().setProperties(SheetProperties().setTitle("Sheet1").setSheetId(0))
        ))
        every { mockSpreadsheets.get("test-spreadsheet-id").execute() } returns spreadsheet

        // Mock the append call
        val appendResponse = AppendValuesResponse()
        every { 
            mockValues.append("test-spreadsheet-id", "Sheet1!A1", any())
                .setValueInputOption("USER_ENTERED")
                .setInsertDataOption("INSERT_ROWS")
                .execute() 
        } returns appendResponse

        // Act
        val result = exporter.export(listOf(property), dryRun = false)

        // Assert
        assertEquals(1, result.insertedCount)
        assertEquals(0, result.updatedCount)
        assertEquals(0, result.failedCount)
    }

    @Test
    fun `export should update existing row when address matches with different case and whitespace`() {
        // Arrange
        val exporter = GoogleSheetsExporter(config, mockSheets)
        val property = Property(
            address = " 123  Main\u00A0St ", // Extra spaces and non-breaking space
            price = "$500,000"
        )

        val existingHeader = listOf("Address", "Price", "Beds", "Baths", "Sqft", "Url", "LastScanned")
        val existingRow = listOf("123 MAIN ST", "$450,000") // Different case
        
        val getResponse = ValueRange().setValues(listOf(existingHeader, existingRow))
        
        every { mockValues.get("test-spreadsheet-id", "Sheet1").execute() } returns getResponse
        
        val spreadsheet = Spreadsheet().setSheets(listOf(
            Sheet().setProperties(SheetProperties().setTitle("Sheet1").setSheetId(0))
        ))
        every { mockSpreadsheets.get("test-spreadsheet-id").execute() } returns spreadsheet

        val batchUpdateResponse = BatchUpdateValuesResponse()
        every { 
            mockValues.batchUpdate("test-spreadsheet-id", any())
                .execute() 
        } returns batchUpdateResponse

        // Act
        val result = exporter.export(listOf(property), dryRun = false)

        // Assert
        assertEquals(0, result.insertedCount)
        assertEquals(1, result.updatedCount)
        
        verify {
            mockValues.batchUpdate("test-spreadsheet-id", any())
        }
    }
}
