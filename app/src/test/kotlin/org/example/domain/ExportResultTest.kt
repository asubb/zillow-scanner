package org.example.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportResultTest {
    @Test
    fun `should create valid export result`() {
        val exportResult = ExportResult(
            insertedCount = 2,
            updatedCount = 1,
            failedCount = 0,
            errors = emptyList()
        )
        
        assertEquals(2, exportResult.insertedCount)
        assertEquals(1, exportResult.updatedCount)
        assertEquals(0, exportResult.failedCount)
        assertEquals(0, exportResult.errors.size)
    }
}
