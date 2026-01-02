# Implementation Plan: Zillow House Scanner CLI

**Branch**: `001-zillow-house-scanner` | **Date**: 2026-01-02 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-zillow-house-scanner/spec.md`

## Summary

Build a CLI application that connects to an open Chrome browser (via CDP/Playwright), extracts property listings from
Zillow search results in list view, and exports them to Google Sheets with duplicate detection and update capability.
The application uses Kotlin with a clean three-layer architecture (Domain/Application/Infrastructure).

## Technical Context

**Language/Version**: Kotlin 2.1+ on JVM 21
**Primary Dependencies**: Playwright for Java/Kotlin (CDP), Google Sheets API v4, Google Auth Library
**Storage**: Google Sheets (external), local config file (JSON/YAML)
**Testing**: Kotlin Test with MockK for mocking
**Target Platform**: macOS/Linux/Windows CLI (JVM-based)
**Project Type**: Single project (Gradle multi-module with `app` subproject)
**Performance Goals**: Scan 20+ listings in <30s, export 20 properties in <10s
**Constraints**: Requires Chrome with `--remote-debugging-port=9222`, Google API credentials
**Scale/Scope**: Single-user CLI tool, ~10-50 properties per scan

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### I. Simplicity First

| Gate                        | Status | Evidence                                                   |
|-----------------------------|--------|------------------------------------------------------------|
| No unnecessary abstractions | PASS   | Direct Playwright calls, no custom framework layers        |
| YAGNI enforced              | PASS   | Only implementing required features (scan, export, update) |
| Solve current problems only | PASS   | No pagination, no scheduling, no multi-browser support     |

### II. Test-First Development

| Gate                            | Status  | Evidence                                  |
|---------------------------------|---------|-------------------------------------------|
| Tests before implementation     | PLANNED | Task sequence requires tests first        |
| External deps isolated          | PLANNED | Interfaces for browser and sheets access  |
| Contract tests for integrations | PLANNED | CDP and Google API mocking via interfaces |

### III. Clean Architecture

| Gate                  | Status  | Evidence                                                                                |
|-----------------------|---------|-----------------------------------------------------------------------------------------|
| Layer separation      | PLANNED | Domain (Property), Application (Scanner, Exporter), Infrastructure (Playwright, Sheets) |
| Dependency direction  | PLANNED | Infrastructure → Application → Domain                                                   |
| Single responsibility | PLANNED | Separate classes for extraction vs export                                               |

**Gate Status**: PASS - All principles can be satisfied with planned approach.

## Project Structure

### Documentation (this feature)

```text
specs/001-zillow-house-scanner/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output (internal contracts, no external API)
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
app/
├── src/main/kotlin/org/example/
│   ├── domain/
│   │   └── Property.kt              # Core data model
│   ├── application/
│   │   ├── PropertyScanner.kt       # Scan orchestration interface
│   │   ├── PropertyExporter.kt      # Export orchestration interface
│   │   └── ScanResult.kt            # Scan result container
│   ├── infrastructure/
│   │   ├── browser/
│   │   │   └── PlaywrightScanner.kt # CDP/Playwright implementation
│   │   ├── sheets/
│   │   │   └── GoogleSheetsExporter.kt # Google Sheets implementation
│   │   └── config/
│   │       └── AppConfig.kt         # Configuration loading
│   └── cli/
│       └── Main.kt                  # CLI entry point
└── src/test/kotlin/org/example/
    ├── domain/
    │   └── PropertyTest.kt
    ├── application/
    │   ├── PropertyScannerTest.kt
    │   └── PropertyExporterTest.kt
    └── integration/
        └── ScanAndExportTest.kt
```

**Structure Decision**: Using existing Gradle single-project structure (`app/`) with Clean Architecture layers under
`org.example`. This aligns with the Constitution's Clean Architecture principle while maintaining simplicity (no
multi-module overhead).

## Complexity Tracking

> No violations - design follows all constitution principles.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|--------------------------------------|
| N/A       | N/A        | N/A                                  |
