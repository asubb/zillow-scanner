# Internal Contracts: Zillow House Scanner CLI

**Date**: 2026-01-02
**Feature**: 001-zillow-house-scanner

This application is a CLI tool with no external API. This document defines the internal interfaces between layers (
Application ↔ Infrastructure).

---

## Application Layer Interfaces

### PropertyScanner

Interface for extracting properties from a browser page.

```kotlin
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
     * @return ScanResult containing extracted properties and metadata
     */
    fun scan(page: Page): ScanResult

    /**
     * Closes the browser connection.
     */
    fun disconnect()
}
```

---

### PropertyExporter

Interface for exporting properties to a spreadsheet.

```kotlin
interface PropertyExporter {
    /**
     * Initializes the exporter with credentials and target spreadsheet.
     * @throws AuthenticationException if credentials are invalid
     * @throws SpreadsheetNotFoundException if spreadsheet ID is invalid
     */
    fun connect()

    /**
     * Reads all existing properties from the spreadsheet.
     * @return Map of address to row number for duplicate detection
     */
    fun readExisting(): Map<String, Int>

    /**
     * Exports properties to the spreadsheet.
     * Updates existing rows (matched by address) or appends new rows.
     * @param properties List of properties to export
     * @param existing Map from readExisting() for duplicate detection
     * @return ExportResult with counts and any errors
     */
    fun export(properties: List<Property>, existing: Map<String, Int>): ExportResult

    /**
     * Closes the API connection.
     */
    fun disconnect()
}
```

---

## Exception Contracts

### BrowserConnectionException

Thrown when Chrome browser connection fails.

| Field   | Type       | Description                       |
|---------|------------|-----------------------------------|
| message | String     | Human-readable error description  |
| port    | Int        | The debug port that was attempted |
| cause   | Throwable? | Underlying exception if any       |

**User-facing message format**:

```
Could not connect to Chrome on port {port}.
Please start Chrome with: --remote-debugging-port={port}
```

---

### AuthenticationException

Thrown when Google Sheets authentication fails.

| Field           | Type       | Description                            |
|-----------------|------------|----------------------------------------|
| message         | String     | Human-readable error description       |
| credentialsPath | String     | Path to credentials file that was used |
| cause           | Throwable? | Underlying exception if any            |

**User-facing message format**:

```
Google Sheets authentication failed.
Please check your credentials file: {credentialsPath}
```

---

### SpreadsheetNotFoundException

Thrown when the configured spreadsheet cannot be accessed.

| Field         | Type   | Description                           |
|---------------|--------|---------------------------------------|
| message       | String | Human-readable error description      |
| spreadsheetId | String | The spreadsheet ID that was not found |

**User-facing message format**:

```
Could not access spreadsheet: {spreadsheetId}
Please verify the ID and ensure the service account has access.
```

---

## CLI Commands

### scan

Scans the current Zillow tab and displays results.

```
zillow-scanner scan [--json]

Options:
  --json    Output in JSON format instead of table

Exit codes:
  0  Success
  1  Chrome not running or connection failed
  2  No Zillow tab found
  3  Extraction error
```

### export

Scans and exports to Google Sheets.

```
zillow-scanner export [--dry-run] [--config <path>]

Options:
  --dry-run      Show what would be exported without writing
  --config       Path to config file (default: ~/.zillow-scanner/config.yaml)

Exit codes:
  0  Success
  1  Chrome connection failed
  2  No Zillow tab found
  3  Google Sheets authentication failed
  4  Export error
```

---

## Configuration Contract

YAML configuration file schema:

```yaml
# Required
google:
  credentials_file: string  # Path to service account JSON
  spreadsheet_id: string    # Google Sheets ID (from URL)
  sheet_name: string        # Default: "Properties"

# Optional
export:
  fields: string[]          # Default: all fields
  # Valid values: address, price, beds, baths, sqft, url, last_scanned

chrome:
  debug_port: integer       # Default: 9222
```
