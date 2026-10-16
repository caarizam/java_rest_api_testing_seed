# Session state

Last updated: 2026-10-15. Branch: `develop`.

## Goal
API test seed (Java + Serenity + Cucumber) targeting the MonkeyTower FastAPI service at
`C:\Users\Usuario\Documents\xdev\monkey_tower_py` (port 9000, routes under `/api/v1/...`).

## Done so far
- **pom.xml**: Java 21, Serenity 5.3.11, Cucumber 7.34.2 (JUnit Platform engine), JUnit 6.1.3 (BOM), Logback 1.6.5,
  Datafaker 2.7.0, Jackson 2.22.3, Failsafe/Surefire 3.6.0. Tests run through Failsafe (`**/TestRunner.java`), Surefire is
  skipped, Serenity `aggregate` runs in `post-integration-test`. Profile `jdk23-unsafe-warning` adds
  `--sun-misc-unsafe-memory-access=allow` on JDK 23+.
- **Secrets & Configuration**: `PropertiesReaderHelper` resolves env var (`API_KEY`) > `test-application.local.properties` (git-ignored)
  > `test-application.properties`. `CommonSteps` sends `x-api-key` and masks `x-api-key` and `Authorization`
  (RestAssured `blacklistHeaders`).
- **Single Register Optimization**: `single_register=true` in `test-application.properties`. `LoginSteps` registers and logs in a user
  only once per feature file (`if (!singleRegister || !Constants.getInstance().isAuthenticated())`), and `Hooks` clears tokens on feature transition (`finishCurrentFeature()`).
- **Positive Tests (`TasksManagement.feature`)**:
  - Full task lifecycle (@e2e @tasks): user registration, task creation (201), task retrieval in list (200), and task deletion (204).
- **Negative Tests (`NegativeTests.feature` & `NegativeSteps.java`)**:
  - Security (@security): user registration without API key (401), with invalid API key (401), task requests without Bearer token (401).
  - Validation (@validation): user registration with missing required fields (`username`, `password`, `confirm`) returning 422, invalid login credentials returning 401.
  - Task edge cases (@tasks): task creation with negative points returning 422, non-existent task retrieval and deletion returning 404.
- **Verification**: Executed `TestRunner` via `mvn clean verify "-Dcucumber.filter.tags=@negative"` with live MonkeyTower API: **13/13 scenarios passed (0 failures, 0 errors)**.
- **Documentation**:
  - Created `.agentic/.context/project_context.md` covering full architecture, dependencies, security, and commands.
  - Updated `README.md` with project structure, `single_register` description, hooks lifecycle, and negative test filtering.

## Findings worth remembering
- In PowerShell, tag filter arguments must be quoted as a single parameter: `mvn clean verify "-Dcucumber.filter.tags=@negative"`.
- MonkeyTower API returns `401 Unauthorized` for missing or invalid `X-API-Key` headers on `/api/v1/users` and `/api/v1/auth/login`.
- Protected routes (e.g. `/api/v1/tasks`) require both a valid API key and a valid Bearer token.
- `CommonSteps` provides `setHeadersWithoutApiKey()`, `setHeadersWithInvalidApiKey()`, and `setHeadersWithoutBearerToken()` for edge case testing.

## Open items / next steps
1. Extend negative testing to cover prompts, links, and chat endpoints as needed.
2. Commit pending changes to git.
