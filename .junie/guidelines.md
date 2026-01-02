# Project Guidelines: Zillow Scanner

This document provides essential information for developing, building, and testing the Zillow Scanner project.

## 1. Build & Configuration

### Prerequisites
- **Java Development Kit (JDK) 21**: The project is configured to use Java 21 toolchain.
- **Gradle**: The project uses the Gradle wrapper (`./gradlew`).

### Build Instructions
To build the project and compile all source files, run:
```bash
./gradlew build
```

### Configuration
The project uses Gradle with Kotlin DSL (`build.gradle.kts`). Dependencies and plugins are managed here.
- **Kotlin Version**: 2.1+
- **JVM Target**: 21

---

## 2. Testing Information

### Testing Strategy
The project follows **Test-First Development (TDD)** as a core principle. Tests must be written before implementation code for all non-trivial functionality.

### Running Tests
To execute all tests in the project, use:
```bash
./gradlew test
```

To run a specific test class:
```bash
./gradlew test --tests "org.example.AppTest"
```

### Adding New Tests
1. **Location**: Place new tests in `app/src/test/kotlin/` within the appropriate package.
2. **Framework**: Use `kotlin.test` for assertions and `@Test` annotations.
3. **TDD Cycle (Red-Green-Refactor)**:
   - Write a failing test.
   - Implement minimal code to pass the test.
   - Refactor while ensuring the test remains green.

### Example Test
```kotlin
class MyNewTest {
    @Test
    fun `should verify simple logic`() {
        val result = 2 + 2
        assertEquals(4, result)
    }
}
```

---

## 3. Development Guidelines

### Core Principles
Adherence to these principles from the Project Constitution is mandatory:

1.  **Simplicity First**:
    - Favor the simplest solution that satisfies requirements.
    - Avoid over-engineering and premature optimization (YAGNI).
2.  **Clean Architecture**:
    - **Domain**: Core data models (no framework dependencies).
    - **Application**: Business logic and orchestration.
    - **Infrastructure**: External integrations (Playwright, Google APIs, CLI).
    - **Dependency Rule**: Infrastructure → Application → Domain. Never reverse.
3.  **Spec-Kit Workflow**:
    - Features should be developed using the Spec-Kit process:
        - `spec.md`: Functional requirements and user stories.
        - `plan.md`: Technical architecture and implementation phases.
        - `tasks.md`: Discrete, trackable tasks for implementation.
    - Consistency between these artifacts is maintained via `/speckit.analyze`.

### Code Style
- Follow standard Kotlin coding conventions.
- Maintain clear separation of concerns between layers.
- Document complex logic, but prefer self-documenting code.

### Browser Automation
- Use **Playwright** for Java/Kotlin for CDP connections and browser interactions.
- Ensure CDP interactions are isolated behind testable interfaces in the Infrastructure layer.
