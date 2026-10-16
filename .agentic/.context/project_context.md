# Project Context: Java REST API Testing Seed

## 1. Project Overview

**java_rest_api_testing_seed** is a modular, behavior-driven development (BDD) test automation framework in Java for REST APIs. It is configured to test the **MonkeyTower FastAPI service** (`/api/v1/...`) and serves as an extensible seed/template for RESTful API automated testing.

The framework combines **Cucumber** for human-readable specification in Gherkin, **Serenity BDD** for enhanced living documentation and REST Assured orchestration, and **JUnit Platform Suite** for test execution.

---

## 2. Technology Stack & Dependencies

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (JDK) | `21+` | Core programming language (compatible with JDK 21 through 23+) |
| **BDD Engine** | Cucumber (`cucumber-junit-platform-engine`) | `7.34.2` | Gherkin scenario parser and step runner |
| **BDD & Reporting** | Serenity BDD (`core`, `junit`, `rest-assured`, `cucumber`) | `5.3.11` | Living documentation, step lifecycle, and REST Assured wrapper |
| **Test Engine** | JUnit Jupiter + JUnit Platform Suite | `6.1.3` (BOM) | Modern JUnit 5 test platform and test runner suite |
| **HTTP Client** | Serenity REST Assured | `5.3.11` | HTTP request construction, execution, and validation |
| **Serialization** | Jackson Databind | `2.22.3` | JSON parsing, node modeling, and DTO object mapping |
| **Data Generation** | Datafaker | `2.7.0` | Dynamic generation of randomized test data (emails, passwords) |
| **Logging** | SLF4J + Logback (`logback-classic`) | `1.6.5` | Structured console and file-based logging |
| **Build Tool** | Apache Maven | `3.9+` | Build lifecycle, dependency resolution, and test execution |

---

## 3. Project Structure

```
java_rest_api_testing_seed/
├── .agentic/
│   └── .context/
│       ├── project_context.md             # High-level architecture and system context (this file)
│       └── session_state.md               # Working notes, history, and active development state
├── src/
│   └── test/
│       ├── java/org/aut/
│       │   ├── common/
│       │   │   └── Constants.java         # Singleton holding in-memory session tokens and state flags
│       │   ├── dto/
│       │   │   ├── AccessToken.java       # POJO for access, refresh tokens and token type
│       │   │   ├── ClientRegistrationDto.java # DTO for client registration table mapping
│       │   │   └── Task.java              # Jackson DTO mapping task payload and response attributes
│       │   ├── helpers/
│       │   │   ├── JsonReaderHelper.java  # Utility for reading local JSON resource files
│       │   │   ├── PropertiesReaderHelper.java # Cascading property resolver (Env > Local > Default)
│       │   │   └── RegistrationHelper.java# Datafaker helper for mock emails, passwords, and MD5 hashing
│       │   ├── steps/definition/
│       │   │   ├── BaseSteps.java         # Base step providing base URL and property reader instance
│       │   │   ├── CommonSteps.java       # Reusable Serenity HTTP requests, header & mask configuration
│       │   │   ├── HealthSteps.java       # Step definitions for health check endpoints
│       │   │   ├── Hooks.java             # Suite, feature boundary detection, and step-level hooks
│       │   │   ├── LoginSteps.java        # Step definitions for user registration and authentication
│       │   │   └── TasksSteps.java        # Step definitions for task creation, retrieval, and deletion
│       │   └── test/
│       │       └── TestRunner.java        # JUnit Platform @Suite configuring Cucumber engine and plugins
│       └── resources/
│           ├── features/
│           │   ├── NegativeTests.feature  # Negative scenarios (e.g., API key validation edge cases)
│           │   └── TasksManagement.feature# End-to-end task lifecycle scenarios (@e2e, @tasks)
│           ├── get_openapijson.sh         # Helper shell script to fetch API OpenAPI specification
│           ├── logback-test.xml           # Logback configuration for console and log file output
│           ├── openapi.json               # (Git-ignored) Local copy of OpenAPI specification
│           ├── test-application.properties# Base application configuration and endpoint routes
│           ├── test-application.local.properties.example # Template for developer local secrets
│           └── test-application.local.properties         # (Git-ignored) Developer local override properties
├── pom.xml                                # Maven build descriptor and plugin configurations
└── README.md                              # User and developer guide
```

---

## 4. Key Design Patterns & Architecture

### 4.1. Modern JUnit Platform Suite & Cucumber 7
- Test execution runs through `org.aut.test.TestRunner`, annotated with `@Suite`, `@IncludeEngines("cucumber")`, and `@SelectPackages("features")`.
- `maven-surefire-plugin` is intentionally skipped (`<skip>true</skip>`), delegating integration test execution exclusively to `maven-failsafe-plugin` targeting `**/TestRunner.java`.
- `serenity-maven-plugin` executes `aggregate` during the `post-integration-test` phase to produce HTML reports in `target/site/serenity/index.html`.
- On JDK 23+, Maven profile `jdk23-unsafe-warning` passes `--sun-misc-unsafe-memory-access=allow` to prevent ByteBuddy warnings.

### 4.2. Configuration Resolution Hierarchy
The `PropertiesReaderHelper` resolves properties using a strict cascading precedence:
1. **Environment Variables**: Property keys are converted to uppercase with hyphens replaced by underscores (e.g. `api-key` -> `API_KEY`, `port` -> `PORT`).
2. **Local Properties**: `src/test/resources/test-application.local.properties` (git-ignored, designed for local developer credentials).
3. **Default Properties**: `src/test/resources/test-application.properties` checked into the repository.

### 4.3. Credential & Secret Masking
- The framework enforces secret protection via Serenity REST Assured's `LogConfig.blacklistHeaders(...)`, masking `x-api-key` and `Authorization` headers with `****` in console logs and generated Serenity reports.
- Passwords and raw credential payloads are excluded from `INFO` logs.

### 4.4. State Management & Lifecycle Hooks
- **Token Management**: `Constants.getInstance()` maintains access tokens, refresh tokens, and active user credentials in memory during execution.
- **Single Register Optimization (`single_register=true`)**:
  - `Hooks.java` intercepts feature boundaries by comparing `scenario.getUri()` against the active feature URI (`@Before(order = 0)`).
  - When `single_register=true`, `Constants.getInstance().setShouldICreateAUser(true)` is enabled only for the first scenario of a feature file. Subsequent scenario outlines or scenarios in that feature reuse the authenticated session, reducing test run times and superfluous user creation.
- **Suite Teardown**: `@AfterAll` in `Hooks.java` clears stored access tokens in `Constants` and closes open feature contexts.

---

## 5. Endpoints & Features Under Test

Targeting the MonkeyTower service (default base URL: `http://127.0.0.1:9000`):

| Property Key | Default Path | Method | Description |
| :--- | :--- | :--- | :--- |
| `auth` | `/api/v1/auth/login` | `POST` | Authenticates client and returns JWT access & refresh tokens |
| `auth_verify` | `/api/v1/auth/verify` | `GET`/`POST` | Verifies validity of the active token |
| `auth_refresh` | `/api/v1/auth/refresh` | `POST` | Refreshes the expired JWT access token |
| `users` | `/api/v1/users` | `POST` | Registers a new user account |
| `tasks` | `/api/v1/tasks` | `GET`, `POST` | Fetches task list or creates a new task |
| `tasks` + `/{id}` | `/api/v1/tasks/{id}` | `DELETE` | Deletes an existing task by identifier |
| `health-check` | `/api/health/service` | `GET` | Health status probe |

---

## 6. Execution Commands

```bash
# Run all E2E tests and generate the Serenity report
mvn clean verify

# Run scenarios with a specific Cucumber tag
mvn clean verify -Dcucumber.filter.tags="@tasks"
mvn clean verify -Dcucumber.filter.tags="@negative"
mvn clean verify -Dcucumber.filter.tags="@e2e"

# Override port or API key from the command line
mvn clean verify -DPORT=9000 -DAPI_KEY=your_secret_key
```

---

## 7. Current Project Status & Notes
- **Tasks E2E Flow**: Fully implemented in `TasksSteps.java` (creation, verification in returned list, and deletion with status code assertion `204`).
- **Feature Outline**: `TasksManagement.feature` exercises multiple tasks via Scenario Outline.
- **Negative Testing**: `NegativeTests.feature` contains draft scenarios for missing API keys.
- **Single Register**: Enabled by default (`single_register=true`) to optimize test suite speed.
