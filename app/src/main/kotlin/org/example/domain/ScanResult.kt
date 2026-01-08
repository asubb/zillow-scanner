package org.example.domain

import java.time.Instant

@kotlinx.serialization.Serializable
data class ScanResult(
    val properties: List<Property>,
    val sourceUrl: String,
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val scannedAt: Instant,
    val extractedCount: Int,
    val errorCount: Int
)
