package org.example.infrastructure.browser

import com.microsoft.playwright.*
import io.mockk.*
import org.example.domain.BrowserConnectionException
import kotlin.test.*

class PlaywrightScannerTest {
    private lateinit var playwright: Playwright
    private lateinit var browser: Browser
    private lateinit var context: BrowserContext
    private lateinit var page: Page

    @BeforeTest
    fun setup() {
        playwright = mockk(relaxed = true)
        browser = mockk(relaxed = true)
        context = mockk(relaxed = true)
        page = mockk(relaxed = true)

        mockkStatic(Playwright::class)
        every { Playwright.create() } returns playwright
        every { playwright.chromium().connectOverCDP(any()) } returns browser
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `connect should throw BrowserConnectionException when connection fails`() {
        val port = 9222
        every { playwright.chromium().connectOverCDP("http://localhost:$port") } throws Exception("Connection failed")

        val scanner = PlaywrightScanner(port)
        
        assertFailsWith<BrowserConnectionException> {
            scanner.connect()
        }
    }

    @Test
    fun `connect should succeed when browser responds`() {
        val port = 9222
        val scanner = PlaywrightScanner(port)
        
        scanner.connect()
        
        verify { playwright.chromium().connectOverCDP("http://localhost:$port") }
    }

    @Test
    fun `findZillowPage should return page when zillow tab found`() {
        val scanner = PlaywrightScanner(9222)
        scanner.connect()

        every { browser.contexts() } returns listOf(context)
        every { context.pages() } returns listOf(page)
        every { page.url() } returns "https://www.zillow.com/homes/for_sale/"

        val result = scanner.findZillowPage()
        
        assertEquals(page, result)
    }

    @Test
    fun `findZillowPage should return null when no zillow tab found`() {
        val scanner = PlaywrightScanner(9222)
        scanner.connect()

        every { browser.contexts() } returns listOf(context)
        every { context.pages() } returns listOf(page)
        every { page.url() } returns "https://www.google.com"

        val result = scanner.findZillowPage()
        
        assertNull(result)
    }

    @Test
    fun `scanCurrentPage should extract property data correctly`() {
        val scanner = PlaywrightScanner(9222)
        val card = mockk<Locator>(relaxed = true)
        val cards = mockk<Locator>(relaxed = true)
        
        every { page.locator("div[data-testid='property-card-data']") } returns cards
        every { cards.count() } returns 1
        every { cards.nth(0) } returns card
        
        every { card.locator("address").innerText() } returns "123 Main St"
        every { card.locator("[data-test='property-card-price']").innerText() } returns "$500,000"
        every { card.locator("a[data-test='property-card-link']").getAttribute("href") } returns "/homedetails/123-Main-St"
        
        val detailsList = mockk<Locator>(relaxed = true)
        every { card.locator("ul[data-testid='property-card-details'] li") } returns detailsList
        every { detailsList.count() } returns 3
        every { detailsList.nth(0).innerText() } returns "3 bds"
        every { detailsList.nth(1).innerText() } returns "2 ba"
        every { detailsList.nth(2).innerText() } returns "1,500 sqft"

        val result = scanner.scan(page, allPages = false)
        
        assertEquals(1, result.extractedCount)
        val property = result.properties[0]
        assertEquals("123 Main St", property.address)
        assertEquals("$500,000", property.price)
        assertEquals(3, property.beds)
        assertEquals(2.0, property.baths)
        assertEquals(1500, property.sqft)
        assertEquals("https://www.zillow.com/homedetails/123-Main-St", property.url)
    }
}
