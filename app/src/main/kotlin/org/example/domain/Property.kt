package org.example.domain

import java.time.Instant

@kotlinx.serialization.Serializable
data class Property(
    val address: String,
    val price: String? = null,
    val beds: Int? = null,
    val baths: Double? = null,
    val sqft: Int? = null,
    val url: String? = null,
    @kotlinx.serialization.Serializable(with = InstantSerializer::class)
    val lastScanned: Instant = Instant.now()
) {
    init {
        require(address.isNotBlank()) { "address MUST NOT be blank" }
    }
}
