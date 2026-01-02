# Feature Specification: Zillow House Scanner CLI

**Feature Branch**: `001-zillow-house-scanner`
**Created**: 2026-01-02
**Status**: Draft
**Input**: User description: "Create an application that allows me to fetch the information on the houses from the zillow, zillow should be accessed via opened chrome browser tab and grab whatever houses are available on the screen (use the list view). The information should be stored in the google spreadsheet, update the information if the house is already on the list. It is a command line application"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Scan Houses from Browser (Priority: P1)

As a real estate researcher, I want to scan house listings from an open Zillow tab in my browser so that I can quickly capture property information without manual data entry.

**Why this priority**: This is the core functionality - without the ability to read house data from the browser, no other features work. It delivers immediate value by automating data capture.

**Independent Test**: Can be fully tested by opening a Zillow search results page in Chrome (with debug port enabled), running the CLI scan command, and verifying the output displays property data in the terminal.

**Acceptance Scenarios**:

1. **Given** Chrome is running with remote debugging enabled and a Zillow list view is open, **When** I run the scan command, **Then** the system displays a list of all visible properties with their key information (address, price, beds, baths, square footage).
2. **Given** Chrome is running but no Zillow tab is open, **When** I run the scan command, **Then** the system displays a clear error message indicating no Zillow page was found.
3. **Given** Chrome is not running or debug port is unavailable, **When** I run the scan command, **Then** the system displays an error explaining how to start Chrome with the required flag.

---

### User Story 2 - Export to Google Sheets (Priority: P2)

As a real estate researcher, I want to export scanned house data to a Google Spreadsheet so that I can maintain a persistent record and share it with others.

**Why this priority**: Depends on P1 (scanning). Provides persistence and sharing capability, transforming single-use scans into a useful database.

**Independent Test**: Can be tested by running the export command with mock property data and verifying a new row appears in the configured Google Spreadsheet.

**Acceptance Scenarios**:

1. **Given** I have scanned property data and configured Google Sheets credentials, **When** I run the export command, **Then** each property is added as a new row in the spreadsheet with all captured fields.
2. **Given** I have not configured Google Sheets credentials, **When** I run the export command, **Then** the system prompts me to set up credentials and provides instructions.
3. **Given** the export encounters a network error, **When** the export fails, **Then** the system displays which properties failed to export and offers to retry.

---

### User Story 3 - Update Existing Listings (Priority: P3)

As a real estate researcher, I want the system to update existing property records instead of creating duplicates so that my spreadsheet stays organized and reflects current prices.

**Why this priority**: Depends on P2 (export). Enhances data quality by preventing duplicates and tracking price changes over time.

**Independent Test**: Can be tested by exporting a property, changing its price in the mock data, re-exporting, and verifying the existing row was updated rather than a new row created.

**Acceptance Scenarios**:

1. **Given** a property already exists in the spreadsheet (matched by address), **When** I export the same property with updated information, **Then** the existing row is updated with new values (price, days on market, etc.).
2. **Given** a property already exists with identical information, **When** I export it again, **Then** only the "last scanned" timestamp is updated.
3. **Given** a property is not found in the spreadsheet, **When** I export it, **Then** a new row is created.

---

### Edge Cases

- What happens when the Zillow page is still loading? System waits for the listing content to be available or times out after a reasonable period with an error message.
- What happens when the spreadsheet has reached its row limit? System warns the user and stops exporting, suggesting they create a new sheet.
- What happens when Zillow's page structure changes? System reports which fields could not be extracted and continues with available data.
- How does the system handle properties with missing data (e.g., price not listed)? Those fields are left empty in the export, not blocking the entire property.
- What happens during a scan if the user scrolls the page? Current scan captures the state at scan initiation; user must re-scan to capture newly visible properties.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST connect to a running Chrome instance via its remote debugging port.
- **FR-002**: System MUST identify browser tabs containing Zillow URLs (zillow.com domain).
- **FR-003**: System MUST extract property listings from a Zillow search results page in list view format.
- **FR-004**: System MUST handle multi-page pagination to scan all available search results.
- **FR-005**: System MUST capture for each property: address, list price, number of bedrooms, number of bathrooms, square footage, and Zillow URL.
- **FR-006**: System MUST display extracted data to the terminal in a human-readable format.
- **FR-007**: System MUST export property data to a Google Spreadsheet.
- **FR-008**: System MUST match existing properties by address to prevent duplicates.
- **FR-009**: System MUST update existing spreadsheet rows when a matching property is found.
- **FR-010**: System MUST add a timestamp column indicating when each property was last scanned.
- **FR-011**: System MUST provide clear error messages when Chrome connection fails.
- **FR-012**: System MUST provide clear error messages when Google Sheets authentication fails.
- **FR-013**: System MUST allow configuration of which property fields to export (e.g., user can choose to exclude square footage). Column names match field names and export order is fixed.

### Key Entities

- **Property**: A real estate listing with address (unique identifier), list price, bedroom count, bathroom count, square footage, Zillow URL, and last scanned timestamp.
- **Spreadsheet**: The target Google Spreadsheet containing property rows, identified by spreadsheet ID and sheet name.
- **ScanResult**: A collection of properties extracted from a single scan operation, with metadata about scan time and source URL.

## Assumptions

- User will start Chrome with `--remote-debugging-port=9222` flag before running the scanner.
- User has a Google account and will set up Google Sheets API credentials (OAuth2 or service account).
- Zillow list view displays at least: address, price, beds, baths, and square footage for each property.
- The spreadsheet uses the first row as headers.
- Address is a reliable unique identifier for matching properties (variations like "123 Main St" vs "123 Main Street" may cause duplicates).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can scan and view 20+ property listings in under 30 seconds.
- **SC-002**: Users can export scanned data to Google Sheets in under 10 seconds per 20 properties.
- **SC-003**: 95% of visible property cards on a Zillow list view page are successfully extracted.
- **SC-004**: Duplicate detection correctly identifies and updates existing properties 99% of the time.
- **SC-005**: Users can complete the full workflow (scan + export) on first attempt with provided documentation.
