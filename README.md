# Zillow House Scanner CLI

A Kotlin-based command-line tool that extracts property listings from an active Zillow browser tab and exports them to Google Sheets.

## Features

- **Browser Integration**: Connects to your existing Chrome browser via Chrome DevTools Protocol (CDP).
- **Smart Extraction**: Scrapes property details (address, price, beds, baths, sqft, and URL) from Zillow search results.
- **Google Sheets Export**: Automatically appends or updates property data in a Google Spreadsheet.
- **Deduplication**: Detects existing listings by address to prevent duplicate entries.
- **Flexible Output**: View results in a formatted table or export as JSON.

## Prerequisites

- **Java 21**: The project requires JDK 21.
- **Google Chrome**: Must be running with remote debugging enabled.
- **Google Cloud Service Account**: Required for exporting to Google Sheets.

## Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd zillow-scanner
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

## Configuration

Create a `config.yaml` file (default location: `~/.zillow-scanner/config.yaml`):

```yaml
google:
  credentials_file: "path/to/your/service-account.json"
  spreadsheet_id: "your-google-spreadsheet-id"
  sheet_name: "Properties"

chrome:
  debug_port: 9222
```

## User Guide

### 1. Start Chrome with Remote Debugging

The scanner needs to "attach" to an existing Chrome window. Close all Chrome instances and restart it from the terminal:

**macOS:**
```bash
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222
```

**Windows:**
```powershell
& "C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222
```

### 2. Navigate to Zillow

Open a tab in the debugging Chrome instance and go to Zillow. Perform a search for homes (e.g., "Homes for sale in Austin, TX") and ensure you are on the list view.

### 3. Run the Scanner

#### Scan Only (Preview)
To see what properties are found without exporting (single page):
```bash
./gradlew run --args="scan"
```

To scan all available pages:
```bash
./gradlew run --args="scan --all-pages"
```

To get JSON output:
```bash
./gradlew run --args="scan --json"
```

#### Export to Google Sheets
To scan and save results to your spreadsheet:
```bash
./gradlew run --args="export"
```

Use `--dry-run` to see what would be changed without writing to the sheet:
```bash
./gradlew run --args="export --dry-run"
```

## Development

### Running Tests
```bash
./gradlew test
```

### Project Structure
- `domain`: Core data models (`Property`, `ScanResult`).
- `application`: Business logic interfaces.
- `infrastructure`: Implementation details (Playwright, Google Sheets, Config).
- `cli`: Command-line interface definitions.

## License
MIT
