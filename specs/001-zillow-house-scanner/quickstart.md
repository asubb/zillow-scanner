# Quickstart: Zillow House Scanner CLI

**Date**: 2026-01-02
**Feature**: 001-zillow-house-scanner

This guide walks you through setting up and using the Zillow House Scanner CLI.

---

## Prerequisites

- Java 21 or later
- Google Chrome browser
- Google Cloud account (for Sheets API)

---

## Step 1: Google Sheets Setup

### 1.1 Create a Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Google Sheets API**:
    - Navigate to **APIs & Services > Library**
    - Search for "Google Sheets API"
    - Click **Enable**

### 1.2 Create Service Account

1. Go to **IAM & Admin > Service Accounts**
2. Click **Create Service Account**
3. Name it (e.g., "zillow-scanner")
4. Click **Create and Continue**
5. Skip role assignment, click **Done**
6. Click on the created service account
7. Go to **Keys** tab
8. Click **Add Key > Create new key > JSON**
9. Save the downloaded JSON file (e.g., `~/.zillow-scanner/service-account.json`)

### 1.3 Create Target Spreadsheet

1. Go to [Google Sheets](https://sheets.google.com/)
2. Create a new spreadsheet
3. Name it (e.g., "Zillow Properties")
4. Add headers in row 1: `Address | Price | Beds | Baths | Sqft | URL | Last Scanned`
5. Copy the spreadsheet ID from the URL:
   ```
   https://docs.google.com/spreadsheets/d/[SPREADSHEET_ID]/edit
   ```

### 1.4 Share Spreadsheet with Service Account

1. Open your spreadsheet
2. Click **Share**
3. Add the service account email (find it in the JSON file: `client_email`)
4. Grant **Editor** access

---

## Step 2: Configuration

Create the configuration file at `config.yaml` (or `~/.zillow-scanner/config.yaml`). **Note**: Use absolute paths for file locations.

```yaml
googleCredentialsFile: "/Users/yourname/.zillow-scanner/service-account.json"
spreadsheetId: "YOUR_SPREADSHEET_ID_HERE"
sheetName: "Sheet1"
chromeDebugPort: 9222
exportFields:
  - address
  - price
  - beds
  - baths
  - sqft
  - url
  - lastScanned
```

---

## Step 3: Start Chrome with Debug Port

Close any existing Chrome instances, then start Chrome with remote debugging:

**macOS**:

```bash
/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222
```

**Linux**:

```bash
google-chrome --remote-debugging-port=9222
```

**Windows**:

```cmd
"C:\Program Files\Google\Chrome\Application\chrome.exe" --remote-debugging-port=9222
```

---

## Step 4: Build the Application

```bash
cd /path/to/zillow-scanner
./gradlew build
```

---

## Step 5: Run the Scanner

### Scan and Display

1. Open Chrome and navigate to Zillow search results (list view)
2. Run the scan command:

```bash
./gradlew run --args="scan"
```

**Expected output**:

```
Scanned: https://www.zillow.com/san-francisco-ca/
Found 20 properties (0 errors)
--------------------------------------------------------------------------------
Address                                  | Price           | Bds/Ba/Sqft     | URL
------------------------------------------------------------------------------------------------------------------------
123 Main St, San Francisco, CA           | $1,200,000      | 3/2/1,450       | https://www.zillow.com/homedetails/...
456 Oak Ave, San Francisco, CA           | $950,000        | 2/1/980         | https://www.zillow.com/homedetails/...
...
```

### Scan and Export

```bash
./gradlew run --args="export"
```

**Expected output**:

```
Connecting to Chrome on port 9222...
Scanning properties...
Found 20 properties.
Exporting to Google Sheets (Spreadsheet ID: ...)...
Successfully exported properties.
  Inserted: 15
  Updated:  5
```

### Dry Run (Preview)

```bash
./gradlew run --args="export --dry-run"
```

This shows what would be exported without making changes.

---

## Troubleshooting

### "Could not connect to Chrome on port 9222"

- Ensure Chrome is running with `--remote-debugging-port=9222`
- Close all existing Chrome instances before starting with the flag
- Check that port 9222 is not blocked by firewall

### "No Zillow tab found"

- Open a Zillow search results page in the Chrome instance
- Ensure the URL contains `zillow.com`
- Use list view (not map view) for best results

### "Google Sheets authentication failed"

- Verify the credentials file path in config
- Ensure the JSON file is valid (download a new key if needed)
- Check that Google Sheets API is enabled in your project

### "Could not access spreadsheet"

- Verify the spreadsheet ID is correct
- Ensure the spreadsheet is shared with the service account email
- Check that the service account has Editor permission

---

## Validation Checklist

After setup, verify each step works:

- [ ] Chrome starts with `--remote-debugging-port=9222`
- [ ] `scan` command connects and displays Chrome tabs
- [ ] Opening Zillow search results → `scan` extracts properties
- [ ] `export --dry-run` shows properties to be exported
- [ ] `export` successfully writes to Google Sheets
- [ ] Re-running `export` updates existing rows (no duplicates)
