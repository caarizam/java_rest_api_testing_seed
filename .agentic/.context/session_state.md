# Session state

Last updated: 2026-10-12. Branch: `refactor`. Uncommitted at the time of writing: `Hooks.java`, `Constants.java`,
`TasksManagement.feature`, `README.md` and this file. The assistant made no commits.

## Goal
API test seed (Java + Serenity + Cucumber) targeting the MonkeyTower FastAPI service at
`C:\Users\Usuario\Documents\xdev\monkey_tower_py` (port 9000, routes under `/api/v1/...`).

## Done so far
- **pom.xml**: Java 21, Serenity 5.3.11, Cucumber 7.34.2 (JUnit Platform engine), JUnit 6.1.3 (BOM), Logback 1.6.5,
  Datafaker 2.7.0, Jackson 2.22.3, Failsafe/Surefire 3.6.0. Tests run through Failsafe (`**/TestRunner.java`), Surefire is
  skipped, Serenity `aggregate` runs in `post-integration-test`. Profile `jdk23-unsafe-warning` adds
  `--sun-misc-unsafe-memory-access=allow` on JDK 23+ (silences the ByteBuddy `Unsafe` warning; a newer ByteBuddy did not help).
- **Migration** from Cucumber 1 / JUnit 4 APIs: `io.cucumber.*` imports, `net.serenitybdd.annotations`,
  `Assertions` (message is the last argument), `TestRunner` is a `@Suite` with `@SelectPackages("features")`.
- **Secrets**: `PropertiesReaderHelper` resolves env var (`API_KEY`) > `test-application.local.properties` (git-ignored)
  > `test-application.properties`. `CommonSteps` sends `x-api-key` and masks `x-api-key` and `Authorization`
  (RestAssured `blacklistHeaders`); verified masked in the Serenity report.
- **Logging**: SLF4J + `logback-test.xml` (console + `target/logs/test-execution.log`, `LOG_LEVEL` env, default INFO).
  All `System.out`/`printStackTrace` replaced. The login body (password) is never logged.
- **Hooks** (`steps/definition/Hooks.java`): BeforeAll, AfterAll, Before (feature start/finish detection via the scenario
  URI, reads the `Feature:` line from the file), BeforeStep, AfterStep. Static state, sequential runs only.
- **DTO/helpers**: `Task` (snake_case via `@JsonProperty`, timestamps as String), `RegistrationHelper.getRegisterMap()`
  (random email `@example.com`, alphanumeric password 6-11), `TasksSteps` parses the task list into `List<Task>`.
- **Scripts**: `src/test/resources/get_openapijson.sh` saves `openapi.json` next to itself (both git-ignored).
- **README.md** rewritten with all of the above.

## Findings worth remembering
- The MonkeyTower API serves `/openapi.json` only with `ENVIRONMENT=development` (`app.py`); the default is production
  (404). Its `.env` has no `ENVIRONMENT`. The Java `api-key` matches the API's `X_API_KEY`.
- The API has no `/api/health/service` route (found by grep), so `HealthSteps` and `health-check` in the properties are stale.
- The MCP server of monkey_tower_py listens on port 9100: avoid that port for stubs.
- Without the real API, runs only prove the framework works; throwaway Java stub servers were used
  (`PORT=<n> mvn clean verify` overrides the port). Do not run `taskkill /IM java.exe`, it kills every Java process.
- `TasksSteps.aClientCanSeeATask` parses the list but does not yet assert the created task is in it, and
  `aClientCanDeleteATask` is still empty. The last user-provided run showed all 3 `@tasks` scenarios passing
  (hooks ran once each, in the right order).

## Open items / next steps
1. Start the API with `ENVIRONMENT=development`, run `get_openapijson.sh`, and align DTOs and steps with the real spec.
2. Add the assertion that the created task is returned, and implement the task delete step.
3. Decide what to do with the health check (remove the step and property, or add a route).
4. Optional: log feature tags in `Hooks`; make `BeforeAll` fail fast when the API is unreachable.
5. Commit the pending changes.

## Conventions
- Match the existing code style (4-space indent, `LOGGER` constants, steps extend `BaseSteps`).
- Secrets only in env vars or `*.local.properties`; never print passwords, tokens or the API key.
