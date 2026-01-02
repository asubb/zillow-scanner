<!--
SYNC IMPACT REPORT
==================
Version change: N/A → 1.0.0 (initial ratification)
Modified principles: N/A (initial version)
Added sections:
  - Core Principles (3): Simplicity First, Test-First Development, Clean Architecture
  - Technology Stack
  - Development Workflow
  - Governance
Removed sections: N/A
Templates requiring updates:
  - .specify/templates/plan-template.md ✅ (Constitution Check section compatible)
  - .specify/templates/spec-template.md ✅ (User stories & requirements align)
  - .specify/templates/tasks-template.md ✅ (Test-first workflow compatible)
Follow-up TODOs: None
-->

# Zillow Scanner Constitution

## Core Principles

### I. Simplicity First

Every implementation choice MUST favor the simplest solution that satisfies requirements.

- **YAGNI Enforcement**: Features, abstractions, and configurability MUST NOT be added until explicitly needed.
- **Minimal Abstractions**: Direct, straightforward code is preferred over patterns and indirection.
  Use abstractions only when they demonstrably reduce complexity.
- **Solve Current Problems**: Implementation MUST address the immediate requirement without
  pre-optimizing for hypothetical future needs.
- **Rationale**: The Zillow Scanner is a focused tool with clear requirements. Over-engineering
  delays delivery and increases maintenance burden without proportional benefit.

### II. Test-First Development

Tests MUST be written before implementation code for all non-trivial functionality.

- **Red-Green-Refactor**: The TDD cycle is mandatory:
  1. Write a failing test that defines expected behavior
  2. Implement the minimum code to make the test pass
  3. Refactor while keeping tests green
- **Test Scope**: Contract tests for CDP interactions and Google API integrations; unit tests for
  data extraction logic; integration tests for end-to-end workflows.
- **No Implementation Without Tests**: Code that cannot be tested (due to external dependencies)
  MUST be isolated behind testable interfaces.
- **Rationale**: CDP and API integrations are prone to silent failures. Tests provide confidence
  that the scanner correctly handles Zillow page structures and exports data accurately.

### III. Clean Architecture

Code MUST maintain clear separation between layers with explicit boundaries.

- **Layer Separation**:
  - **Domain**: Core data models (Property, Listing) with no framework dependencies
  - **Application**: Business logic (extraction, transformation, export orchestration)
  - **Infrastructure**: External integrations (CDP/Playwright, Google APIs, CLI)
- **Dependency Direction**: Infrastructure depends on Application; Application depends on Domain.
  Never the reverse.
- **Single Responsibility**: Each class/module MUST have one clear purpose. Extraction logic
  MUST NOT be mixed with export logic.
- **Rationale**: Zillow's page structure may change. Clean boundaries allow updating selectors
  without affecting export logic, and swapping export targets without touching extraction code.

## Technology Stack

- **Language**: Kotlin 2.1+ on JVM 21
- **Build System**: Gradle with Kotlin DSL
- **Browser Automation**: Playwright for Java/Kotlin (CDP connection)
- **API Integration**: Google Drive API, Google Sheets API
- **Testing**: Kotlin test framework, MockK for mocking
- **Configuration**: External configuration file (JSON/YAML) for column mapping

## Development Workflow

### Code Review Requirements

- All changes MUST be reviewed against the three core principles before merge
- Reviewers MUST verify: no unnecessary complexity added, tests precede implementation,
  layer boundaries respected

### Quality Gates

- All tests MUST pass before merge
- New functionality MUST include corresponding tests
- Principle violations MUST be documented with explicit justification if exceptions are needed

### Change Process

1. Create feature branch from main
2. Write failing tests for new behavior
3. Implement minimal code to pass tests
4. Refactor if needed while maintaining green tests
5. Submit for review with principle compliance noted

## Governance

This constitution establishes the fundamental development principles for the Zillow Scanner project.

- **Supremacy**: These principles supersede ad-hoc decisions. When in doubt, refer to this document.
- **Amendments**: Changes to this constitution require:
  1. Written proposal with rationale
  2. Impact assessment on existing code
  3. Migration plan if principles are modified
  4. Version increment per semantic versioning rules
- **Compliance Review**: PRs SHOULD reference which principles guided implementation decisions.
- **Exceptions**: Temporary deviations MUST be documented in code comments with a TODO for
  resolution and tracked in the Complexity Tracking section of the implementation plan.

**Version**: 1.0.0 | **Ratified**: 2026-01-02 | **Last Amended**: 2026-01-02
