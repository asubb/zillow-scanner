package org.example.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanResultTest {
    @Test
    fun `should create valid scan result`() {
        val now = Instant.now()
        val properties = listOf(
            Property(address = "123 Main St", lastScanned = now)
        )
        val scanResult = ScanResult(
            properties = properties,
            sourceUrl = "https://www.zillow.com/homes/for_sale/",
            scannedAt = now,
            extractedCount = 1,
            errorCount = 0
        )
        
        assertEquals(properties, scanResult.properties)
        assertEquals("https://www.zillow.com/homes/for_sale/", scanResult.sourceUrl)
        assertEquals(now, scanResult.scannedAt)
        assertEquals(1, scanResult.extractedCount)
        assertEquals(0, scanResult.errorCount)
    }
}
