package org.example.integration

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.infrastructure.browser.PlaywrightScanner
import org.example.infrastructure.config.AppConfig
import org.example.infrastructure.sheets.GoogleSheetsExporter
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanAndExportTest {
    private val config = AppConfig(
        spreadsheetId = "test-spreadsheet-id",
        sheetName = "Sheet1",
        googleCredentialsFile = "test-creds.json",
        exportFields = listOf("address", "price", "beds", "baths", "sqft", "url")
    )

    @Test
    fun `should scan properties from playwright and export to google sheets`() {
        // --- Setup Mocks for Playwright ---
        val mockPage = mockk<Page>()
        val mockCards = mockk<Locator>()
        val mockCard = mockk<Locator>()
        val mockAddress = mockk<Locator>()
        val mockPrice = mockk<Locator>()
        val mockLink = mockk<Locator>()
        val mockDetails = mockk<Locator>()
        val mockDetailItem = mockk<Locator>()

        every { mockPage.url() } returns "https://www.zillow.com/homes/for_sale/"
        every { mockPage.locator("div[data-testid='property-card-data']") } returns mockCards
        every { mockCards.count() } returns 1
        every { mockCards.nth(0) } returns mockCard
        
        every { mockCard.locator("address") } returns mockAddress
        every { mockAddress.innerText() } returns "123 Main St"
        
        every { mockCard.locator("[data-test='property-card-price']") } returns mockPrice
        every { mockPrice.innerText() } returns "$500,000"
        
        every { mockCard.locator("a[data-test='property-card-link']") } returns mockLink
        every { mockLink.getAttribute("href") } returns "/homedetails/123-Main-St/123_zpid/"
        
        every { mockCard.locator("ul[data-testid='property-card-details'] li") } returns mockDetails
        every { mockDetails.count() } returns 3
        every { mockDetails.nth(0) } returns mockDetailItem
        every { mockDetails.nth(1) } returns mockDetailItem
        every { mockDetails.nth(2) } returns mockDetailItem
        every { mockDetailItem.innerText() } returnsMany listOf("3 bd", "2 ba", "1,500 sqft")

        // --- Setup Mocks for Google Sheets ---
        val mockSheets = mockk<Sheets>()
        val mockSpreadsheets = mockk<Sheets.Spreadsheets>()
        val mockValues = mockk<Sheets.Spreadsheets.Values>()
        val mockGet = mockk<Sheets.Spreadsheets.Values.Get>()
        val mockAppend = mockk<Sheets.Spreadsheets.Values.Append>()
        val mockSpreadsheetGet = mockk<Sheets.Spreadsheets.Get>()

        every { mockSheets.spreadsheets() } returns mockSpreadsheets
        every { mockSpreadsheets.values() } returns mockValues
        
        // Mock getting spreadsheet to find sheet ID
        val spreadsheet = Spreadsheet().setSheets(listOf(
            Sheet().setProperties(SheetProperties().setTitle("Sheet1").setSheetId(0))
        ))
        every { mockSpreadsheets.get("test-spreadsheet-id") } returns mockSpreadsheetGet
        every { mockSpreadsheetGet.execute() } returns spreadsheet

        // Mock reading existing values (empty sheet)
        every { mockValues.get("test-spreadsheet-id", "Sheet1") } returns mockGet
        every { mockGet.execute() } returns ValueRange().setValues(null)

        // Mock update for initial data (headers + first row)
        val mockUpdate = mockk<Sheets.Spreadsheets.Values.Update>()
        every { mockValues.update("test-spreadsheet-id", "Sheet1!A1", any()) } returns mockUpdate
        every { mockUpdate.setValueInputOption("USER_ENTERED") } returns mockUpdate
        every { mockUpdate.execute() } returns UpdateValuesResponse()

        // Mock appending new values (for subsequent calls if any)
        every { mockValues.append("test-spreadsheet-id", "Sheet1!A1", any()) } returns mockAppend
        every { mockAppend.setValueInputOption("USER_ENTERED") } returns mockAppend
        every { mockAppend.setInsertDataOption("INSERT_ROWS") } returns mockAppend
        every { mockAppend.execute() } returns AppendValuesResponse()

        // Mock batchUpdate for formatting
        val mockBatchUpdate = mockk<Sheets.Spreadsheets.BatchUpdate>()
        every { mockSpreadsheets.batchUpdate("test-spreadsheet-id", any()) } returns mockBatchUpdate
        every { mockBatchUpdate.execute() } returns BatchUpdateSpreadsheetResponse()

        // --- Execution ---
        
        // 1. Scan
        val scanner = PlaywrightScanner(9222)
        // We bypass connect() and findZillowPage() because they require a real browser.
        // We directly call scan() with our mock page.
        val scanResult = scanner.scan(mockPage, allPages = false)
        
        // 2. Export
        val exporter = GoogleSheetsExporter(config, mockSheets)
        val exportResult = exporter.export(scanResult.properties, dryRun = false)

        // --- Verification ---
        assertEquals(1, scanResult.extractedCount)
        assertEquals("123 Main St", scanResult.properties[0].address)
        assertEquals("$500,000", scanResult.properties[0].price)
        
        assertEquals(1, exportResult.insertedCount)
        assertEquals(0, exportResult.updatedCount)
        
        verify {
            mockValues.update("test-spreadsheet-id", "Sheet1!A1", withArg {
                val values = it.getValues()
                assertEquals(2, values.size) // Headers + 1 row
                val headers = values[0]
                val row = values[1]
                assertEquals("Address", headers[0])
                assertEquals("123 Main St", row[0])
                assertEquals(500000.0, row[1]) // Cleaned price
                assertEquals(3, row[2]) // beds
                assertEquals(2.0, row[3]) // baths
                assertEquals(1500, row[4]) // sqft
                assertEquals("https://www.zillow.com/homedetails/123-Main-St/123_zpid/", row[5])
            })
        }
    }
}
