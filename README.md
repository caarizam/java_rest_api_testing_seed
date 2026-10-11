# Automation Seed Framework: Java + Serenity REST Assured + Cucumber

A base project for BDD-style REST API testing. Scenarios are written in Gherkin and executed with Cucumber, requests go through Serenity's REST Assured wrapper, and Serenity produces the HTML reports.

## Stack

| Tool | Version |
|------|---------|
| Java | 21 |
| Serenity BDD (core, junit, cucumber, rest-assured) | 5.3.11 |
| Cucumber (via Serenity) | 7.x |
| JUnit | 4.13.2 |
| Jackson Databind | 2.22.3 |
| Datafaker | 2.7.0 |
| Maven | 3.9+ |

## Project structure

```
src/test/java/org/aut
├── common/            Constants: singleton that stores access tokens between steps
├── dto/               Request payload classes (e.g. ClientRegistrationDto)
├── helpers/           Properties and JSON file readers
├── steps/definition/  Cucumber step definitions (BaseSteps, CommonSteps, ClientSteps, HealthSteps)
└── test/              TestRunner (Cucumber + Serenity JUnit runner)
src/test/resources
├── features/          Gherkin feature files
└── test-application.properties   Base URL, port and endpoint paths
```

## Configuration

Edit `src/test/resources/test-application.properties` to point at the API under test:

```properties
url-base=http://127.0.0.1
port=9000
auth=/api/auth/login
add-user=/api/user
delete-user=/api/user
health-check=/api/health/service
```

## Running the tests

Requires JDK 21 or newer and Maven.

```bash
# run the tests and generate the Serenity report
mvn clean verify

# run only certain scenarios by Cucumber tag
mvn clean verify -Dcucumber.filter.tags="@registration"
```

The tests run in the `integration-test` phase through `maven-failsafe-plugin` (`TestRunner`). The Surefire plugin is disabled. The Serenity report is aggregated automatically after the tests, and the entry point is `target/site/serenity/index.html`.

## Adding a new test

1. Add a scenario to a `.feature` file under `src/test/resources/features/` (tag it so it can be filtered).
2. Implement missing steps in `org.aut.steps.definition`, extending `BaseSteps` to get `getBaseUrl()` and the properties reader.
3. Put reusable HTTP calls in `CommonSteps` and payload models in `org.aut.dto`.
4. Add any new endpoint path to `test-application.properties`.

The `TestRunner` currently selects scenarios tagged `@e2e`.

## API key and secrets

The API key is sent in the `x-api-key` header on every request. It is resolved in this order:

1. Environment variable `API_KEY` (use this in CI)
2. `src/test/resources/test-application.local.properties` (git-ignored, for local development)
3. `api-key` in `test-application.properties` (left empty on purpose)

To set it up locally, copy `test-application.local.properties.example` to `test-application.local.properties` and set `api-key`. The same lookup applies to every property: any key can be overridden by an upper-cased environment variable with `-` replaced by `_`.

`x-api-key` and `Authorization` header values are masked (`****`) in the console output and in the Serenity report. To mask more headers, add them to the `blacklistHeaders` set in `CommonSteps`.
