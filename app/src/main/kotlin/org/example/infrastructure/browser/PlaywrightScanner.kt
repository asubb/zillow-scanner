package org.example.infrastructure.browser

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.application.PropertyScanner
import org.example.domain.BrowserConnectionException
import org.example.domain.Property
import org.example.domain.ScanResult
import java.time.Instant

private val logger = KotlinLogging.logger {}

class PlaywrightScanner(private val port: Int) : PropertyScanner {
    private var playwright: Playwright? = null
    private var browser: Browser? = null

    override fun connect() {
        try {
            playwright = Playwright.create()
            browser = playwright?.chromium()?.connectOverCDP("http://localhost:$port")
        } catch (e: Exception) {
            throw BrowserConnectionException("Could not connect to Chrome on port $port. Please start Chrome with: --remote-debugging-port=$port", e)
        }
    }

    override fun findZillowPage(): Page? {
        val context = browser?.contexts()?.firstOrNull() ?: return null
        return context.pages().firstOrNull { page ->
            page.url().contains("zillow.com", ignoreCase = true)
        }
    }

    override fun scan(page: Page, allPages: Boolean): ScanResult {
        val allProperties = mutableListOf<Property>()
        var totalErrorCount = 0
        val startTime = Instant.now()
        val sourceUrl = page.url()

        do {
            val (properties, errorCount) = scanCurrentPage(page)
            allProperties.addAll(properties)
            totalErrorCount += errorCount

            if (!allPages) break

            val nextButton = page.locator("nav[role='navigation'][aria-label='Pagination'] a[rel='next']")
            val hasNext = nextButton.count() > 0 && nextButton.getAttribute("aria-disabled") != "true"
            
            if (hasNext) {
                val currentUrl = page.url()
                nextButton.click()
                
                // Wait for the URL to change to ensure we are moving to a new page
                try {
                    page.waitForCondition({ page.url() != currentUrl }, Page.WaitForConditionOptions().setTimeout(5000.0))
                } catch (e: Exception) {
                    // Fallback to a hard wait if condition times out
                    page.waitForTimeout(2000.0)
                }
                
                // Wait for property cards to be present and stable on the new page
                page.waitForSelector("div[data-testid='property-card-data']", Page.WaitForSelectorOptions().setTimeout(5000.0))
                page.waitForTimeout(1000.0) // Brief stabilization wait
            }
        } while (hasNext && allPages)

        return ScanResult(
            properties = allProperties,
            sourceUrl = sourceUrl,
            scannedAt = startTime,
            extractedCount = allProperties.size,
            errorCount = totalErrorCount
        )
    }

    private fun scanCurrentPage(page: Page): Pair<List<Property>, Int> {
        val properties = mutableListOf<Property>()
        var errorCount = 0
        
        val cards = page.locator("div[data-testid='property-card-data']")
        val count = cards.count()
        logger.debug { "Found $count property cards on current page" }
        
        for (i in 0 until count) {
            try {
                val card = cards.nth(i)
                val address = card.locator("address").innerText()
                logger.debug { "Scanning property: $address" }
                val price = try { card.locator("[data-test='property-card-price']").innerText() } catch (e: Exception) { null }
                val url = try { 
                    val href = card.locator("a[data-test='property-card-link']").getAttribute("href")
                    if (href?.startsWith("/") == true) "https://www.zillow.com$href" else href
                } catch (e: Exception) { null }

                val details = card.locator("ul[data-testid='property-card-details'] li")
                val detailsCount = details.count()
                
                var beds: Int? = null
                var baths: Double? = null
                var sqft: Int? = null

                for (j in 0 until detailsCount) {
                    val text = details.nth(j).innerText().lowercase()
                    when {
                        text.contains("bd") || text.contains("bed") -> {
                            beds = text.replace(Regex("[^0-9]"), "").toIntOrNull()
                        }
                        text.contains("ba") || text.contains("bath") -> {
                            baths = text.replace(Regex("[^0-9.]"), "").toDoubleOrNull()
                        }
                        text.contains("sqft") -> {
                            sqft = text.replace(Regex("[^0-9]"), "").toIntOrNull()
                        }
                    }
                }
                
                properties.add(Property(
                    address = address,
                    price = price,
                    beds = beds,
                    baths = baths,
                    sqft = sqft,
                    url = url,
                    lastScanned = Instant.now()
                ))
            } catch (e: Exception) {
                errorCount++
            }
        }
        return properties to errorCount
    }

    override fun disconnect() {
        browser?.close()
        playwright?.close()
    }
}
