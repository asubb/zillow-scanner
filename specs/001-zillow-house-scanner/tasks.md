# Tasks: Zillow House Scanner CLI

**Input**: Design documents from `/specs/001-zillow-house-scanner/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Included per Constitution (Test-First Development principle)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Project root**: `app/src/main/kotlin/org/example/`
- **Tests**: `app/src/test/kotlin/org/example/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependency configuration

- [ ] T001 Add Playwright dependency to app/build.gradle.kts
- [ ] T002 [P] Add Google Sheets API dependencies to app/build.gradle.kts
- [ ] T003 [P] Add kaml YAML dependency to app/build.gradle.kts
- [ ] T004 [P] Add MockK test dependency to app/build.gradle.kts
- [ ] T005 Create directory structure: app/src/main/kotlin/org/example/domain/
- [ ] T006 [P] Create directory structure: app/src/main/kotlin/org/example/application/
- [ ] T007 [P] Create directory structure: app/src/main/kotlin/org/example/infrastructure/browser/
- [ ] T008 [P] Create directory structure: app/src/main/kotlin/org/example/infrastructure/sheets/
- [ ] T009 [P] Create directory structure: app/src/main/kotlin/org/example/infrastructure/config/
- [ ] T010 [P] Create directory structure: app/src/main/kotlin/org/example/cli/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core domain model and configuration that all user stories depend on

**CRITICAL**: No user story work can begin until this phase is complete

- [ ] T011 Write test for Property data class in app/src/test/kotlin/org/example/domain/PropertyTest.kt
- [ ] T012 Create Property data class in app/src/main/kotlin/org/example/domain/Property.kt
- [ ] T013 [P] Write test for ScanResult data class in app/src/test/kotlin/org/example/domain/ScanResultTest.kt
- [ ] T014 [P] Create ScanResult data class in app/src/main/kotlin/org/example/domain/ScanResult.kt
- [ ] T015 [P] Write test for ExportResult data class in app/src/test/kotlin/org/example/domain/ExportResultTest.kt
- [ ] T016 [P] Create ExportResult data class in app/src/main/kotlin/org/example/domain/ExportResult.kt
- [ ] T017 Write test for AppConfig loading in app/src/test/kotlin/org/example/infrastructure/config/AppConfigTest.kt
- [ ] T018 Create AppConfig data class and loader in app/src/main/kotlin/org/example/infrastructure/config/AppConfig.kt
- [ ] T019 [P] Create BrowserConnectionException in app/src/main/kotlin/org/example/domain/Exceptions.kt
- [ ] T020 [P] Create AuthenticationException in app/src/main/kotlin/org/example/domain/Exceptions.kt
- [ ] T021 [P] Create SpreadsheetNotFoundException in app/src/main/kotlin/org/example/domain/Exceptions.kt

**Checkpoint**: Foundation ready - domain model and config available for all user stories

---

## Phase 3: User Story 1 - Scan Houses from Browser (Priority: P1)

**Goal**: Connect to Chrome via CDP, find Zillow tab, extract property listings, display in terminal

**Independent Test**: Run `scan` command with Chrome open to Zillow, verify property data displays in terminal

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T022 [P] [US1] Write unit test for PropertyScanner interface in
  app/src/test/kotlin/org/example/application/PropertyScannerTest.kt
- [ ] T023 [P] [US1] Write unit test for PlaywrightScanner.connect() in
  app/src/test/kotlin/org/example/infrastructure/browser/PlaywrightScannerTest.kt
- [ ] T024 [P] [US1] Write unit test for PlaywrightScanner.findZillowPage() in
  app/src/test/kotlin/org/example/infrastructure/browser/PlaywrightScannerTest.kt
- [ ] T025 [P] [US1] Write unit test for PlaywrightScanner.scan() in
  app/src/test/kotlin/org/example/infrastructure/browser/PlaywrightScannerTest.kt

### Implementation for User Story 1

- [ ] T026 [US1] Create PropertyScanner interface in app/src/main/kotlin/org/example/application/PropertyScanner.kt
- [ ] T027 [US1] Implement PlaywrightScanner.connect() in
  app/src/main/kotlin/org/example/infrastructure/browser/PlaywrightScanner.kt
- [ ] T028 [US1] Implement PlaywrightScanner.findZillowPage() in
  app/src/main/kotlin/org/example/infrastructure/browser/PlaywrightScanner.kt
- [ ] T029 [US1] Implement PlaywrightScanner.scan() with DOM extraction in
  app/src/main/kotlin/org/example/infrastructure/browser/PlaywrightScanner.kt
- [ ] T030 [US1] Implement PlaywrightScanner.disconnect() in
  app/src/main/kotlin/org/example/infrastructure/browser/PlaywrightScanner.kt
- [ ] T031 [US1] Create ScanCommand with table output in app/src/main/kotlin/org/example/cli/ScanCommand.kt
- [ ] T032 [US1] Add --json flag support to ScanCommand in app/src/main/kotlin/org/example/cli/ScanCommand.kt
- [ ] T033 [US1] Wire ScanCommand into Main.kt in app/src/main/kotlin/org/example/cli/Main.kt
- [ ] T034 [US1] Add error handling for Chrome connection failure in app/src/main/kotlin/org/example/cli/ScanCommand.kt
- [ ] T035 [US1] Add error handling for no Zillow tab found in app/src/main/kotlin/org/example/cli/ScanCommand.kt

**Checkpoint**: User Story 1 complete - `scan` command works independently

---

## Phase 4: User Story 2 - Export to Google Sheets (Priority: P2)

**Goal**: Export scanned properties to Google Sheets, append new rows

**Independent Test**: Run `export` command, verify new rows appear in configured spreadsheet

### Tests for User Story 2

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T036 [P] [US2] Write unit test for PropertyExporter interface in
  app/src/test/kotlin/org/example/application/PropertyExporterTest.kt
- [ ] T037 [P] [US2] Write unit test for GoogleSheetsExporter.connect() in
  app/src/test/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporterTest.kt
- [ ] T038 [P] [US2] Write unit test for GoogleSheetsExporter.readExisting() in
  app/src/test/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporterTest.kt
- [ ] T039 [P] [US2] Write unit test for GoogleSheetsExporter.export() append in
  app/src/test/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporterTest.kt

### Implementation for User Story 2

- [ ] T040 [US2] Create PropertyExporter interface in app/src/main/kotlin/org/example/application/PropertyExporter.kt
- [ ] T041 [US2] Implement GoogleSheetsExporter.connect() with service account auth in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T042 [US2] Implement GoogleSheetsExporter.readExisting() in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T043 [US2] Implement GoogleSheetsExporter.export() for appending new rows in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T044 [US2] Implement GoogleSheetsExporter.disconnect() in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T045 [US2] Create ExportCommand in app/src/main/kotlin/org/example/cli/ExportCommand.kt
- [ ] T046 [US2] Add --dry-run flag support to ExportCommand in app/src/main/kotlin/org/example/cli/ExportCommand.kt
- [ ] T047 [US2] Add --config flag support to ExportCommand in app/src/main/kotlin/org/example/cli/ExportCommand.kt
- [ ] T048 [US2] Wire ExportCommand into Main.kt in app/src/main/kotlin/org/example/cli/Main.kt
- [ ] T049 [US2] Add error handling for Google Sheets auth failure in
  app/src/main/kotlin/org/example/cli/ExportCommand.kt

**Checkpoint**: User Story 2 complete - `export` command works independently (append only)

---

## Phase 5: User Story 3 - Update Existing Listings (Priority: P3)

**Goal**: Detect existing properties by address, update rows instead of creating duplicates

**Independent Test**: Export same property twice, verify row is updated (not duplicated)

### Tests for User Story 3

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T050 [P] [US3] Write unit test for duplicate detection logic in
  app/src/test/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporterTest.kt
- [ ] T051 [P] [US3] Write unit test for GoogleSheetsExporter.export() update in
  app/src/test/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporterTest.kt

### Implementation for User Story 3

- [ ] T052 [US3] Implement address matching in GoogleSheetsExporter.readExisting() in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T053 [US3] Implement row update logic in GoogleSheetsExporter.export() in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt
- [ ] T054 [US3] Update ExportCommand to display update vs insert counts in
  app/src/main/kotlin/org/example/cli/ExportCommand.kt
- [ ] T055 [US3] Add timestamp update on re-scan in
  app/src/main/kotlin/org/example/infrastructure/sheets/GoogleSheetsExporter.kt

**Checkpoint**: User Story 3 complete - deduplication works, existing rows are updated

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and documentation

- [ ] T056 [P] Write integration test for full scan+export workflow in
  app/src/test/kotlin/org/example/integration/ScanAndExportTest.kt
- [ ] T057 [P] Create sample config.yaml in docs/sample-config.yaml
- [ ] T058 Validate quickstart.md instructions work end-to-end
- [ ] T059 Update application.mainClass in app/build.gradle.kts to org.example.cli.MainKt
- [ ] T060 Run full test suite and fix any failures

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion
- **User Story 2 (Phase 4)**: Depends on Foundational completion (can run parallel to US1)
- **User Story 3 (Phase 5)**: Depends on User Story 2 completion (extends export logic)
- **Polish (Phase 6)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Independent - scan works without export
- **User Story 2 (P2)**: Uses Property from US1, but can be tested with mock data
- **User Story 3 (P3)**: Extends US2 export logic - depends on US2 completion

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Interfaces before implementations
- Core logic before CLI integration
- Error handling after happy path

### Parallel Opportunities

- Setup tasks T002-T004 can run in parallel
- Directory creation T006-T010 can run in parallel
- Domain model tests T013, T015 can run in parallel
- Domain model implementations T014, T016 can run in parallel
- US1 tests T022-T025 can run in parallel
- US2 tests T036-T039 can run in parallel
- US3 tests T050-T051 can run in parallel

---

## Parallel Example: User Story 1 Tests

```bash
# Launch all US1 tests together:
Task: "Write unit test for PropertyScanner interface"
Task: "Write unit test for PlaywrightScanner.connect()"
Task: "Write unit test for PlaywrightScanner.findZillowPage()"
Task: "Write unit test for PlaywrightScanner.scan()"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test `scan` command with real Zillow page
5. User can already capture property data (MVP value!)

### Incremental Delivery

1. Setup + Foundational → Foundation ready
2. User Story 1 → `scan` command works → MVP!
3. User Story 2 → `export` command works → Persistence!
4. User Story 3 → Deduplication works → Production-ready!
5. Each story adds value without breaking previous stories

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
