---
name: api-sdet
description: >-
  Use this skill when designing, implementing, debugging, or executing BDD REST API tests
  in the java_rest_api_testing_seed framework (Java 21, Serenity BDD, REST Assured, Cucumber 7, JUnit 5).
---

# API SDET Skill & Runbook (`api_sdet.md`)

This runbook provides step-by-step procedures and patterns for building and maintaining automated REST API tests in this repository.

---

## 1. Contract Analysis from OpenAPI (`openapi.json`)

Before writing new features or steps, inspect `src/test/resources/openapi.json` to verify the API contract:

1. **Refresh the OpenAPI spec (optional, requires API running with `ENVIRONMENT=development`)**:
   - Run `src/test/resources/get_openapijson.sh` to download the latest `/openapi.json`.
2. **Identify Endpoint Details**:
   - **Path & HTTP Method**: e.g., `POST /api/v1/tasks`, `GET /api/v1/prompts`, `POST /api/v1/links/search`, `GET /api/v1/chats/unread-count`.
   - **Security Schemes**:
     - `APIKeyHeader`: Requires `x-api-key` header on every request.
     - `HTTPBearer`: Requires `Authorization: Bearer <access_token>` header on protected routes.
   - **Request & Response Schemas**:
     - Inspect `components.schemas` for required fields, field types, constraints (`minLength`, `maxLength`, `minimum`, `enum`), and response structures.
   - **Expected Status Codes**:
     - Positive: `200 OK`, `201 Created`, `204 No Content`.
     - Negative: `400 Bad Request`, `401 Unauthorized` (missing/invalid API key or Bearer token), `404 Not Found` (non-existent resource ID), `422 Unprocessable Entity` (`HTTPValidationError` on missing required fields or constraint violations).
3. **Register Endpoint Path**:
   - Add the endpoint key to `src/test/resources/test-application.properties`:
     ```properties
     prompts=/api/v1/prompts
     links=/api/v1/links
     chats=/api/v1/chats
     ```
   - Read it in step classes via `propertiesReaderHelper.getProperty("<key>")`.

---

## 2. Authoring Gherkin Feature Files

Feature files live under `src/test/resources/features/`.

### Tagging Strategy
- `@e2e`: Required at the `Feature:` level (or scenario level) so `TestRunner.java` includes it in default runs.
- Domain tags: `@tasks`, `@prompts`, `@links`, `@chats`, `@auth`.
- Negative/Category tags: `@negative`, `@security`, `@validation`, `@notFound`.

### Template for Positive E2E Scenarios
```gherkin
@e2e
Feature: <Feature Name>

  Background: The user is registered and logged in
    Given The user is registered

  @<domain_tag>
  Scenario Outline: <Scenario description>
    When The user creates the resource "<name>"
    Then The user can see the created resource "<name>"
    And The user can delete the created resource "<name>"

  Examples:
    | name          |
    | Sample Item 1 |
    | Sample Item 2 |
```

### Template for Negative / Edge-Case Scenarios
```gherkin
@e2e @negative
Feature: Negative API Validation and Edge Cases

  @security
  Scenario: Reject request without API key
    When The client registers a new user without the API key
    Then The response status code should be 401

  @tasks @validation
  Scenario: Reject task creation with invalid points
    Given The user is registered
    When The user attempts to create a task with points -1
    Then The response status code should be 422
```

> **Note on `single_register`**: When `single_register=true` in `test-application.properties`, `Hooks` and `LoginSteps` ensure `Given The user is registered` creates and logs in a user only once per feature file (on the first scenario that invokes the step) and reuses the stored Bearer token for subsequent scenarios in that feature.

---

## 3. Implementing Step Definitions & HTTP Calls

Step classes live in `src/test/java/org/aut/steps/definition/`.

### Step Class Pattern
Every step definition class must:
1. Extend `BaseSteps` (provides `getBaseUrl()` and `propertiesReaderHelper`).
2. Inject `CommonSteps` via `@Steps CommonSteps commonSteps;`.
3. Declare an SLF4J logger: `private static final Logger LOGGER = LoggerFactory.getLogger(MySteps.class);`.

```java
package org.aut.steps.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import net.serenitybdd.annotations.Steps;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleSteps extends BaseSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleSteps.class);

    @Steps
    CommonSteps commonSteps;

    private Response response;

    @When("^The user performs an authenticated action$")
    public void userPerformsAuthenticatedAction() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("name", "Example");

        String body = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        String baseUrl = getBaseUrl();
        String endpoint = propertiesReaderHelper.getProperty("tasks");

        commonSteps.setHeadersCredentials();
        this.response = commonSteps.postRequest(baseUrl, endpoint, body);
        Assertions.assertEquals(201, this.response.statusCode(),
                "Expected status 201, but got " + this.response.statusCode());
    }
}
```

### Selecting the Right Header Mode in `CommonSteps`
| Method | `x-api-key` Header | `Authorization` Header | Use Case |
| :--- | :--- | :--- | :--- |
| `commonSteps.setHeadersCredentials()` | Valid API Key | `Bearer <last_token>` | Authenticated endpoints (`/api/v1/tasks`, `/api/v1/prompts`, etc.) |
| `commonSteps.setHeadersBasic()` | Valid API Key | Omitted | Public endpoints requiring only API key (`/api/v1/users`, `/api/v1/auth/login`) |
| `commonSteps.setHeadersWithoutApiKey()` | Omitted (`includeApiKey=false`) | Omitted | Negative security test: missing API key (`401`) |
| `commonSteps.setHeadersWithInvalidApiKey(key)` | Custom invalid key | Omitted | Negative security test: invalid API key (`401`) |
| `commonSteps.setHeadersWithoutBearerToken()` | Valid API Key | Omitted | Negative security test: missing JWT token on protected endpoint (`401`) |

---

## 4. DTO Mapping & Test Data Generation

### Creating a DTO (`src/test/java/org/aut/dto/`)
Follow the pattern in `Task.java`:
- Use `@JsonIgnoreProperties(ignoreUnknown = true)` at the class level so newly added backend fields do not break tests.
- Map `snake_case` JSON properties to `camelCase` Java fields with `@JsonProperty("...")`.

```java
package org.aut.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prompt {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("content")
    private String content;
    @JsonProperty("parameter_id")
    private Integer parameterId;
    @JsonProperty("created_at")
    private String createdAt;

    // Default constructor + getters and setters
}
```

### Deserializing JSON Responses
- **Single Object**:
  ```java
  Task task = new ObjectMapper().readValue(response.asString(), Task.class);
  ```
- **List of Objects**:
  ```java
  List<Task> tasks = new ObjectMapper().readValue(response.asString(), new TypeReference<List<Task>>() {});
  ```
- **Quick Path Extraction**:
  ```java
  String token = response.path("access_token").toString();
  ```

### Generating Fake Data (`RegistrationHelper` / Datafaker)
- Use `RegistrationHelper.getRegisterMap()` to generate a random `email` (`[a-z0-9]{10}@example.com`) and `password` (`[a-zA-Z0-9]{6,11}`).
- Use `net.datafaker.Faker` for additional randomized test data.

---

## 5. Verification, Execution & Debugging Workflow

1. **Compile Check**:
   Always run `mvn test-compile` first to catch compilation or import errors quickly.
2. **Targeted Execution**:
   Run the specific tag you created or modified (remember to quote `-D` arguments in PowerShell):
   ```powershell
   mvn clean verify "-Dcucumber.filter.tags=@negative"
   mvn clean verify "-Dcucumber.filter.tags=@tasks"
   ```
3. **Full Suite Execution**:
   ```powershell
   mvn clean verify
   ```
4. **Inspecting Logs & Serenity Reports**:
   - **Execution Log**: `target/logs/test-execution.log` (controlled by `src/test/resources/logback-test.xml`; set env `LOG_LEVEL=DEBUG` locally if deeper trace is needed).
   - **Failsafe Summary**: `target/failsafe-reports/org.aut.test.TestRunner.txt`.
   - **Serenity HTML Report**: `target/site/serenity/index.html`.
