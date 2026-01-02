package org.example.application

import com.microsoft.playwright.Page
import org.example.domain.ScanResult

interface PropertyScanner {
    /**
     * Connects to Chrome browser at the configured debug port.
     * @throws BrowserConnectionException if Chrome is not running or port unavailable
     */
    fun connect()

    /**
     * Finds and returns a page containing a Zillow URL.
     * @return Page if found, null if no Zillow tab is open
     */
    fun findZillowPage(): Page?

    /**
     * Extracts property listings from the given page.
     * @param page Playwright Page object for a Zillow search results page
     * @param allPages if true, attempts to navigate through all pagination pages
     * @return ScanResult containing extracted properties and metadata
     */
    fun scan(page: Page, allPages: Boolean = false): ScanResult

    /**
     * Closes the browser connection.
     */
    fun disconnect()
}
