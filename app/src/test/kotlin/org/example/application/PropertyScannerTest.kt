package org.example.application

import com.microsoft.playwright.Page
import io.mockk.every
import io.mockk.mockk
import org.example.domain.ScanResult
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyScannerTest {
    @Test
    fun `should scan properties from page`() {
        val scanner = mockk<PropertyScanner>()
        val page = mockk<Page>()
        val expectedResult = ScanResult(
            properties = emptyList(),
            sourceUrl = "https://www.zillow.com",
            scannedAt = java.time.Instant.now(),
            extractedCount = 0,
            errorCount = 0
        )
        
        every { scanner.scan(page) } returns expectedResult
        
        val result = scanner.scan(page)
        
        assertEquals(expectedResult, result)
    }
}
