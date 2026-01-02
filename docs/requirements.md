# Zillow Scanner Requirements

## Project Overview

The **Zillow Scanner** is a Kotlin-based application designed to extract real estate listing information (house titles,
prices, and descriptions) from Zillow. Due to Zillow's strict anti-bot measures, the application will not use
traditional scraping methods. Instead, it will connect to an active Google Chrome instance via the Chrome DevTools
Protocol (CDP).

## Functional Requirements

1. **Chrome Connection:**
    - The app must connect to a running instance of Google Chrome started with the `--remote-debugging-port=9222` flag.
    - It should use the **Playwright** library for the CDP connection.

2. **Tab Identification:**
    - The app must iterate through open tabs to find one that contains a Zillow URL (e.g., `zillow.com/homedetails/` or
      search results).

3. **Data Extraction:**
    - **House List:** Extract a list of property cards from a search results page.
    - **Description:** Fetch the full text description of a specific property.
    - **Metadata:** Capture price, address, and key features (beds/baths).

4. **User Interaction:**
    - The app should provide a simple CLI output of the gathered data.

5. **Data Export (Google Sheets):**
    - The app must export the gathered data to a Google Spreadsheet using the Google Drive/Sheets API.
    - It must support updating existing information in the spreadsheet.
    - The list of columns to be exported must be configurable.

## Technical Requirements

- **Language:** Kotlin 2.1+
- **Build System:** Gradle (Kotlin DSL)
- **Library:** Playwright for Java/Kotlin
- **API Integration:** Google Drive API & Google Sheets API
- **JVM:** Java 21
- **Configuration:** External configuration file (e.g., JSON or YAML) to define columns.

## Setup Instructions

1. **Chrome Preparation:**
   Run Chrome from the terminal:
   ```bash
   /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --remote-debugging-port=9222
   ```
2. **Google API Setup:**
   - Enable Google Drive and Google Sheets APIs in the Google Cloud Console.
   - Download the `credentials.json` for a Service Account or OAuth2 client.
3. **Build Application:**
   ```bash
   ./gradlew build
   ```

## Roadmap

- [ ] Initialize project with Playwright and Google API dependencies.
- [ ] Implement CDP connection logic.
- [ ] Create selectors for Zillow property elements.
- [ ] Implement data extraction.
- [ ] Implement Google Sheets export logic.
- [ ] Implement configuration management for column selection.
- [ ] Create final user documentation with available columns.
