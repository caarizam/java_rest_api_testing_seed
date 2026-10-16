# Automation Seed Framework: Java + Serenity REST Assured + Cucumber

A base project for BDD-style REST API testing. Scenarios are written in Gherkin and executed with Cucumber, requests go through Serenity's REST Assured wrapper, and Serenity produces the HTML reports. It is currently pointed at the MonkeyTower FastAPI service (`/api/v1/...`).

## Stack

| Tool | Version |
|------|---------|
| Java | 21 |
| Serenity BDD (core, junit, cucumber, rest-assured) | 5.3.11 |
| Cucumber (via Serenity, JUnit Platform engine) | 7.34.2 |
| JUnit (Jupiter + Platform Suite) | 6.1.3 |
| Logback (SLF4J) | 1.6.5 |
| Jackson Databind | 2.22.3 |
| Datafaker | 2.7.0 |
| Maven | 3.9+ |

## Project structure

```
.agentic/
└── .context/          Project context and session state documentation
src/test/java/org/aut
├── common/            Constants: singleton that stores access tokens and session flags between steps
├── dto/               Payload and response classes (AccessToken, ClientRegistrationDto, Task)
├── helpers/           PropertiesReaderHelper, JsonReaderHelper, RegistrationHelper (fake registration data)
├── steps/definition/  Cucumber glue: BaseSteps, CommonSteps (HTTP calls), LoginSteps, TasksSteps, HealthSteps, Hooks
└── test/              TestRunner (JUnit Platform @Suite that runs the Cucumber engine)
src/test/resources
├── features/          Gherkin feature files (TasksManagement.feature, NegativeTests.feature)
├── test-application.properties   Base URL, port, endpoint paths, and execution flags
├── logback-test.xml   Logging configuration
└── get_openapijson.sh Downloads the API's OpenAPI spec (see below)
```

## Configuration

Edit `src/test/resources/test-application.properties` to point at the API under test:

```properties
# single register per feature
single_register=true

# url-base
url-base=http://127.0.0.1
port=9000

# authentication
auth=/api/v1/auth/login
auth_verify=/api/v1/auth/verify
auth_refresh=/api/v1/auth/refresh

# tasks
tasks=/api/v1/tasks

# add-user, registration
users=/api/v1/users

# health check
health-check=/api/health/service

# api key, set it in test-application.local.properties or the API_KEY environment variable
api-key=
```

- `single_register`: When set to `true`, the framework registers and authenticates a user only on the first scenario of each feature file, reusing the access token for subsequent scenarios in the same feature. Set to `false` if every scenario must register an independent new user.
- Any property can be overridden by an upper-cased environment variable with `-` replaced by `_` (for example `PORT=9100` or `API_KEY=my_key`).

## Running the tests

Requires JDK 21 or newer and Maven.

```bash
# run all tests and generate the Serenity report
mvn clean verify

# run only certain scenarios by Cucumber tag
mvn clean verify -Dcucumber.filter.tags="@tasks"
mvn clean verify -Dcucumber.filter.tags="@negative"
```

The tests run in the `integration-test` phase through `maven-failsafe-plugin` (`TestRunner`, a JUnit Platform `@Suite` that runs the Cucumber engine). The Surefire plugin is disabled. The Serenity report is aggregated automatically after the tests, and the entry point is `target/site/serenity/index.html`.

`TestRunner` currently selects scenarios tagged `@e2e` from the `features` package.

On JDK 23 and later the JVM warns about `sun.misc.Unsafe` (ByteBuddy, used by Serenity). A Maven profile that activates only on JDK 23+ passes `--sun-misc-unsafe-memory-access=allow` to the test JVM to silence it.

## Adding a new test

1. Add a scenario to a `.feature` file under `src/test/resources/features/` (tag it so it can be filtered).
2. Implement missing steps in `org.aut.steps.definition`, extending `BaseSteps` to get `getBaseUrl()` and the properties reader.
3. Put reusable HTTP calls in `CommonSteps` and payload/response models in `org.aut.dto` (use `@JsonProperty` for snake_case fields).
4. Add any new endpoint path to `test-application.properties`.

For fake registration data, `RegistrationHelper.getRegisterMap()` returns a map with a random `email` (`<random>@example.com`) and a random alphanumeric `password` of 6 to 11 characters.

## Hooks

`Hooks` (in the glue package) provides:

| Hook | When | What it does |
|------|------|--------------|
| `@BeforeAll` | once, before the first scenario | logs the target URL and warns if no API key is configured |
| `@AfterAll` | once, after the last scenario | closes the last feature and clears the stored access tokens |
| `@Before(order = 0)` | before each scenario | detects feature boundaries, logs `Feature starting` / `Feature finished`, and manages `single_register` session creation |
| `@BeforeStep` / `@AfterStep` | around each step | `DEBUG` logs, and a `WARN` when the scenario has failed |

Cucumber has no `BeforeFeature`/`AfterFeature` hook, so features are detected from the scenario URI. This relies on static state and on sequential execution; it is not safe for parallel runs. When `single_register=true`, `Hooks` sets `Constants.getInstance().setShouldICreateAUser(true)` only for the first scenario of a feature file, allowing subsequent scenarios to reuse the credentials.

## Logging

Logging uses SLF4J with Logback, configured in `src/test/resources/logback-test.xml`. Output goes to the console and to `target/logs/test-execution.log`. Our own code logs at `INFO`; set the `LOG_LEVEL` environment variable (for example `LOG_LEVEL=DEBUG`) for more detail. Passwords and tokens are not logged at `INFO`. At `DEBUG` the login response (which holds the access token) is logged, so keep `DEBUG` out of CI.

## API key and secrets

The API key is sent in the `x-api-key` header on every request. It is resolved in this order:

1. Environment variable `API_KEY` (use this in CI)
2. `src/test/resources/test-application.local.properties` (git-ignored, for local development)
3. `api-key` in `test-application.properties` (left empty on purpose)

To set it up locally, copy `test-application.local.properties.example` to `test-application.local.properties` and set `api-key`. It must match the API's `X_API_KEY`.

`x-api-key` and `Authorization` header values are masked (`****`) in the console output and in the Serenity report. To mask more headers, add them to the `blacklistHeaders` set in `CommonSteps`.

## OpenAPI spec

`src/test/resources/get_openapijson.sh` downloads `/openapi.json` from the running API using the same API key and saves it as `openapi.json` next to the script (or to a path you pass as the first argument). Set `BASE_URL` to use another host. The script and its output are git-ignored.

The MonkeyTower API only serves `/openapi.json` when it is started with `ENVIRONMENT=development`; in production it returns 404.
