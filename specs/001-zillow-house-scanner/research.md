# Research: Zillow House Scanner CLI

**Date**: 2026-01-02
**Feature**: 001-zillow-house-scanner

## 1. Chrome DevTools Protocol (CDP) Connection via Playwright

### Decision

Use **Playwright for Java** to connect to existing Chrome browser via CDP.

### Rationale

- Playwright provides high-level API for CDP connection via `connectOverCDP()`
- Supports enumerating open tabs and finding specific pages by URL
- Mature library with good documentation and active maintenance
- Single dependency covers browser connection, tab management, and DOM extraction

### Alternatives Considered

| Alternative             | Why Rejected                                               |
|-------------------------|------------------------------------------------------------|
| CDP4j                   | Lower-level, more boilerplate, less maintained             |
| Selenium + ChromeDriver | Requires launching new browser, not connecting to existing |
| Raw CDP via WebSocket   | Too low-level, significant implementation effort           |

### Implementation Details

**Gradle Dependency**:

```kotlin
implementation("com.microsoft.playwright:playwright:1.57.0")
```

**Key API Patterns**:

```kotlin
// Connect to existing Chrome
val playwright = Playwright.create()
val browser = playwright.chromium().connectOverCDP("http://localhost:9222")

// Find Zillow tab
val context = browser.contexts().first()
val zillowPage = context.pages().firstOrNull { page ->
    page.url().contains("zillow.com", ignoreCase = true)
}

// Extract DOM content using Locators
val propertyCards = page.locator("div[data-testid='property-card-data']")
val address = propertyCards.nth(i).locator("address").innerText()
```

---

## 2. Google Sheets API Integration

### Decision

Use **Google Sheets API v4** with **Service Account authentication**.

### Rationale

- Service account authentication is simpler for CLI tools (no OAuth2 user flow)
- Google Sheets API v4 supports read, write, update, and append operations
- Well-documented with official Java client library

### Alternatives Considered

| Alternative      | Why Rejected                                        |
|------------------|-----------------------------------------------------|
| OAuth2 user flow | Requires browser interaction, complex for CLI       |
| CSV export       | No deduplication/update capability, not cloud-based |
| Airtable API     | Third-party service, additional account required    |

### Implementation Details

**Gradle Dependencies**:

```kotlin
implementation("com.google.api-client:google-api-client:2.0.0")
implementation("com.google.apis:google-api-services-sheets:v4-rev20251110-2.0.0")
implementation("com.google.auth:google-auth-library-oauth2-http:1.36.0")
implementation("com.google.http-client:google-http-client-gson:1.45.0")
```

**Key API Patterns**:

```kotlin
// Authentication with service account
val credentials = ServiceAccountCredentials
    .fromStream(FileInputStream("credentials.json"))
    .createScoped(listOf(SheetsScopes.SPREADSHEETS))

val sheetsService = Sheets.Builder(
    GoogleNetHttpTransport.newTrustedTransport(),
    JacksonFactory.getDefaultInstance(),
    HttpCredentialsAdapter(credentials)
).setApplicationName("Zillow Scanner").build()

// Read rows
sheetsService.spreadsheets().values()
    .get(spreadsheetId, "Sheet1!A:Z")
    .execute().getValues()

// Update row
sheetsService.spreadsheets().values()
    .update(spreadsheetId, "Sheet1!A$rowNum:Z$rowNum", valueRange)
    .setValueInputOption("USER_ENTERED")
    .execute()

// Append row
sheetsService.spreadsheets().values()
    .append(spreadsheetId, "Sheet1!A1", valueRange)
    .setValueInputOption("USER_ENTERED")
    .setInsertDataOption("INSERT_ROWS")
    .execute()
```

---

## 3. Zillow DOM Selectors

### Decision

Use CSS selectors targeting Zillow's list view property cards.

### Rationale

- Zillow uses data-test attributes which are more stable than CSS classes
- List view has consistent card structure for property listings
- Playwright's Locator API handles dynamic content well

### Implementation Notes

- Selectors must be discovered by inspecting actual Zillow HTML
- Common patterns: `[data-test='property-card']`, `[data-test='property-address']`
- Fallback: Use `page.evaluate()` to run JavaScript extraction if selectors break
- Graceful degradation: Extract available fields even if some selectors fail

---

## 4. Configuration File Format

### Decision

Use **YAML** for configuration file.

### Rationale

- Human-readable and easy to edit manually
- Supports comments for documentation
- Standard for CLI tool configuration
- Kotlin has good YAML libraries (kaml, snakeyaml)

### Alternatives Considered

| Alternative | Why Rejected                                |
|-------------|---------------------------------------------|
| JSON        | No comments, less human-friendly for config |
| TOML        | Less familiar, fewer Kotlin libraries       |
| Properties  | Limited structure for complex config        |

### Configuration Structure

```yaml
# ~/.zillow-scanner/config.yaml
google:
  credentials_file: /path/to/service-account.json
  spreadsheet_id: "1abc..."
  sheet_name: "Properties"

export:
  fields:
    - address
    - price
    - beds
    - baths
    - sqft
    - url
    - last_scanned

chrome:
  debug_port: 9222
```

**Gradle Dependency**:

```kotlin
implementation("com.charleskorn.kaml:kaml:0.55.0")
```

---

## 5. Duplicate Detection Strategy

### Decision

Match properties by **exact address string**.

### Rationale

- Address is the natural unique identifier for properties
- Zillow URL contains property ID but may change between scans
- Simple string comparison is sufficient for initial version

### Limitations

- Address formatting variations may cause false negatives
- Future enhancement: normalize addresses or use Zillow property ID from URL

---

## Summary of Dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Browser automation
    implementation("com.microsoft.playwright:playwright:1.57.0")

    // Google Sheets API
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20251110-2.0.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.36.0")
    implementation("com.google.http-client:google-http-client-gson:1.45.0")

    // Configuration
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    // Testing
    testImplementation("io.mockk:mockk:1.13.9")
}
```
