# Data Model: Zillow House Scanner CLI

**Date**: 2026-01-02
**Feature**: 001-zillow-house-scanner

## Entities

### Property

The core domain entity representing a real estate listing.

| Field       | Type    | Description                                | Nullable |
|-------------|---------|--------------------------------------------|----------|
| address     | String  | Full street address (unique identifier)    | No       |
| price       | String  | List price as displayed (e.g., "$450,000") | Yes      |
| beds        | Int?    | Number of bedrooms                         | Yes      |
| baths       | Double? | Number of bathrooms (supports half-baths)  | Yes      |
| sqft        | Int?    | Square footage                             | Yes      |
| url         | String  | Full Zillow URL to property detail page    | Yes      |
| lastScanned | Instant | Timestamp of last scan                     | No       |

**Validation Rules**:

- `address` MUST NOT be blank
- `lastScanned` MUST be set on creation and updated on each scan

**Notes**:

- Price stored as String to preserve formatting (commas, dollar sign)
- Nullable numeric fields allow partial data extraction when Zillow page is incomplete

---

### ScanResult

Container for properties extracted from a single scan operation.

| Field          | Type           | Description                                     | Nullable |
|----------------|----------------|-------------------------------------------------|----------|
| properties     | List<Property> | Extracted property listings                     | No       |
| sourceUrl      | String         | URL of the Zillow page scanned                  | No       |
| scannedAt      | Instant        | Timestamp when scan was performed               | No       |
| extractedCount | Int            | Number of properties successfully extracted     | No       |
| errorCount     | Int            | Number of property cards that failed extraction | No       |

---

### AppConfig

Application configuration loaded from YAML file.

| Field                 | Type         | Description                           | Default      |
|-----------------------|--------------|---------------------------------------|--------------|
| googleCredentialsFile | String       | Path to service account JSON          | Required     |
| spreadsheetId         | String       | Google Sheets spreadsheet ID          | Required     |
| sheetName             | String       | Target sheet name within spreadsheet  | "Properties" |
| exportFields          | List<String> | Fields to export (subset of Property) | All fields   |
| chromeDebugPort       | Int          | Chrome remote debugging port          | 9222         |

---

### ExportResult

Result of exporting properties to Google Sheets.

| Field         | Type         | Description                       |
|---------------|--------------|-----------------------------------|
| insertedCount | Int          | Number of new rows added          |
| updatedCount  | Int          | Number of existing rows updated   |
| failedCount   | Int          | Number of exports that failed     |
| errors        | List<String> | Error messages for failed exports |

---

## State Transitions

### Property Lifecycle

```
[Scanned] → [Exported (New)] → [Exported (Updated)]
                 ↑                      ↓
                 └──────────────────────┘
```

1. **Scanned**: Property extracted from Zillow page, exists in memory only
2. **Exported (New)**: First time exported to Google Sheets, new row created
3. **Exported (Updated)**: Re-scanned and matched existing row, row updated

---

## Relationships

```
ScanResult 1 ──────* Property
     │
     └── Contains multiple properties from single scan

AppConfig 1 ──────1 Spreadsheet (external)
     │
     └── Points to target Google Sheet

ExportResult 1 ──────* Property (exported)
     │
     └── Reports on export operation for properties
```

---

## Spreadsheet Schema

The target Google Sheet has the following structure:

| Column | Header       | Source Field         |
|--------|--------------|----------------------|
| A      | Address      | property.address     |
| B      | Price        | property.price       |
| C      | Beds         | property.beds        |
| D      | Baths        | property.baths       |
| E      | Sqft         | property.sqft        |
| F      | URL          | property.url         |
| G      | Last Scanned | property.lastScanned |

**Notes**:

- Row 1 contains headers
- Address (column A) is used for duplicate detection
- Column order is fixed; exportFields config only controls which columns to populate
