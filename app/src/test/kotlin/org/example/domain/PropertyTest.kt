package org.example.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PropertyTest {
    @Test
    fun `should create valid property`() {
        val now = Instant.now()
        val property = Property(
            address = "123 Main St",
            price = "$500,000",
            beds = 3,
            baths = 2.0,
            sqft = 1500,
            url = "https://www.zillow.com/homedetails/123-Main-St",
            lastScanned = now
        )
        
        assertEquals("123 Main St", property.address)
        assertEquals("$500,000", property.price)
        assertEquals(3, property.beds)
        assertEquals(2.0, property.baths)
        assertEquals(1500, property.sqft)
        assertEquals("https://www.zillow.com/homedetails/123-Main-St", property.url)
        assertEquals(now, property.lastScanned)
    }

    @Test
    fun `should fail if address is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Property(
                address = " ",
                price = "$500,000",
                lastScanned = Instant.now()
            )
        }
    }
}
