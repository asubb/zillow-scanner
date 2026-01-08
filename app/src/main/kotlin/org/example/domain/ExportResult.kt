package org.example.domain

data class ExportResult(
    val insertedCount: Int,
    val updatedCount: Int,
    val failedCount: Int,
    val errors: List<String> = emptyList()
)
