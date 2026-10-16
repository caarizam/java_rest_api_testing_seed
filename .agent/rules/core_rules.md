# Core Agent Rules (`core_rules.md`)

These rules govern how AI agents operate within the `java_rest_api_testing_seed` repository. All agents must strictly adhere to the permissions, execution constraints, and coding conventions defined below.

---

## 1. File System & Project Reading Permissions

- **Permitted Read Access**: Agents are explicitly authorized to read, inspect, search, and analyze all project files within the workspace, including:
  - Build and documentation files: `pom.xml`, `README.md`, `.gitignore`
  - Context and customization directories: `.agent/**`, `.agentic/**`
  - Test source code: `src/test/java/org/aut/**`
  - Test resources and specifications: `src/test/resources/features/**`, `src/test/resources/openapi.json`, `src/test/resources/test-application.properties`, `src/test/resources/test-application.local.properties.example`, `src/test/resources/logback-test.xml`
  - Execution logs and reports: `target/logs/test-execution.log`, `target/site/serenity/**`, `target/failsafe-reports/**`
- **Secret & Credential Protection**:
  - **NEVER** print, output, log at `INFO` level, or commit actual secret values from `src/test/resources/test-application.local.properties` or environment variables (`API_KEY`).
  - Always keep `x-api-key` and `Authorization` headers in the `RestAssured` `blacklistHeaders` configuration in `org.aut.steps.definition.CommonSteps` so they remain masked (`****`) in console output and Serenity HTML reports.
  - Never log request bodies that contain plaintext passwords (e.g., login or registration payloads) at `INFO` level.

---

## 2. Git Command Permissions & Restrictions

- **Permitted Git Commands (Read-Only / Diagnostic)**:
  Agents may proactively execute read-only Git commands to inspect repository state and changes:
  - `git status`
  - `git diff` (both unstaged and `--staged`, or scoped to specific files)
  - `git log` (always bound output length, e.g., `git log -n 5` or `git log --oneline -n 10`)
  - `git branch`
  - `git show`
- **Restricted Git Commands (Require Explicit User Confirmation)**:
  Agents must **NOT** execute mutating, history-altering, or remote Git operations unless explicitly instructed by the user in the current turn:
  - `git commit`, `git push`, `git pull`, `git fetch`
  - `git merge`, `git rebase`, `git cherry-pick`
  - `git reset`, `git clean`, `git checkout -- <file>`, `git restore` (discarding working tree changes)

---

## 3. Maven & Test Execution Rules

- **Permitted Maven Commands**:
  Agents are authorized to compile and run the test suite using Maven:
  ```powershell
  # Compile test sources to verify syntax and imports
  mvn test-compile

  # Run all default (@e2e) scenarios and generate the Serenity aggregate report
  mvn clean verify

  # Run specific scenarios filtered by Cucumber tag (quote -D args in Windows PowerShell)
  mvn clean verify "-Dcucumber.filter.tags=@e2e"
  mvn clean verify "-Dcucumber.filter.tags=@tasks"
  mvn clean verify "-Dcucumber.filter.tags=@negative"
  mvn clean verify "-Dcucumber.filter.tags=@security"
  mvn clean verify "-Dcucumber.filter.tags=@validation"
  mvn clean verify "-Dcucumber.filter.tags=@notFound"
  ```
- **Test Runner & Plugin Architecture**:
  - Tests execute in the `integration-test` phase via `maven-failsafe-plugin` targeting `org.aut.test.TestRunner` (JUnit Platform `@Suite` running the `cucumber` engine).
  - `maven-surefire-plugin` is intentionally configured with `<skip>true</skip>`—do not re-enable it.
  - `serenity-maven-plugin` runs `aggregate` in `post-integration-test` to generate `target/site/serenity/index.html`.
  - On JDK 23+, the `jdk23-unsafe-warning` profile automatically supplies `--sun-misc-unsafe-memory-access=allow`.
- **Runtime & Process Safety**:
  - **NEVER** run `taskkill /IM java.exe` or broad process-killing commands, as they terminate all running Java processes on the user's machine.
  - Default target API URL is `http://127.0.0.1:9000` (MonkeyTower FastAPI service).
  - Avoid port `9100` for any mock/stub servers, as port `9100` is used by the MonkeyTower MCP server.

---

## 4. Coding & Framework Conventions

- **Formatting & Style**:
  - Use 4-space indentation, UTF-8 encoding, and Java 21 language features.
  - Preserve existing comments and docstrings unrelated to your changes.
- **Step Definitions (`org.aut.steps.definition`)**:
  - All step definition classes must extend `BaseSteps` to inherit `propertiesReaderHelper` and `getBaseUrl()`.
  - Inject `CommonSteps` using `@Steps CommonSteps commonSteps;` (from `net.serenitybdd.annotations.Steps`).
  - Place reusable HTTP calls (`GET`, `POST`, `PUT`, `DELETE`) in `CommonSteps` annotated with `@Step` (`net.serenitybdd.annotations.Step`).
- **Session & Single-Register Lifecycle**:
  - Respect the `single_register` flag in `test-application.properties`.
  - `Hooks.java` manages feature boundary detection (`@Before(order = 0)`) and clears tokens in `finishCurrentFeature()` and `@AfterAll`.
  - `LoginSteps.aUserIsRegistered()` checks `Constants.getInstance().isShouldICreateAUser()` and sets `setShouldICreateAUser(false)` after creating a user when `single_register=true`.
  - Note: `Hooks` relies on static state for feature boundary tracking; test execution must remain sequential (not parallel).
- **DTOs & Serialization (`org.aut.dto`)**:
  - Annotate DTOs with `@JsonIgnoreProperties(ignoreUnknown = true)` and `@JsonProperty("<snake_case_name>")`.
- **Assertions & Logging**:
  - Use JUnit 5 `org.junit.jupiter.api.Assertions` (where the failure message is the **last** argument).
  - Use SLF4J (`LoggerFactory.getLogger(ClassName.class)`) for all logging; never use `System.out.println` or `printStackTrace()`.
