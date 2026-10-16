# Agent Instructions (`AGENTS.md`)

Welcome to the **`java_rest_api_testing_seed`** repository—a BDD REST API test automation framework built with **Java 21**, **Serenity BDD 5.3.11**, **REST Assured**, **Cucumber 7.34.2**, and **JUnit Platform Suite 6.1.3**, targeting the **MonkeyTower FastAPI service** (`/api/v1/...`).

---

## 1. Required Reading & References

Before performing tasks in this workspace, consult the following agent configuration and context documents:

- **Core Rules & Permissions**: [`.agent/rules/core_rules.md`](./rules/core_rules.md)
  - File reading permissions and secret-handling guardrails.
  - Permitted read-only Git commands (`git status`, `git diff`, `git log`, `git branch`) vs. restricted operations.
  - Maven compilation and test execution rules (`mvn test-compile`, `mvn clean verify`, tag filtering).
  - Coding standards and framework conventions.
- **API SDET Skill & Runbook**: [`.agent/skills/api_sdet.md`](./skills/api_sdet.md)
  - Step-by-step procedures for analyzing `openapi.json`, writing Gherkin `.feature` files, implementing step definitions (`BaseSteps`, `CommonSteps`), creating Jackson DTOs, and running/debugging tests.
- **Project Context & Session State**:
  - [`.agentic/.context/project_context.md`](../.agentic/.context/project_context.md) — High-level architecture, dependency matrix, endpoint inventory, and design patterns.
  - [`.agentic/.context/session_state.md`](../.agentic/.context/session_state.md) — Active session notes, completed milestones, and open next steps.

---

## 2. Project Layout Quick Reference

```text
java_rest_api_testing_seed/
├── .agent/
│   ├── AGENTS.md                          # Workspace agent entry point (this file)
│   ├── rules/
│   │   └── core_rules.md                  # Permissions, Git/Maven rules, and coding standards
│   └── skills/
│       └── api_sdet.md                    # API SDET procedures, templates, and runbooks
├── .agentic/
│   └── .context/
│       ├── project_context.md             # System architecture and project context
│       └── session_state.md               # Working session state and progress tracker
├── src/test/
│   ├── java/org/aut/
│   │   ├── common/Constants.java          # Singleton storing session tokens and single_register state
│   │   ├── dto/                           # Jackson DTOs (AccessToken, ClientRegistrationDto, Task)
│   │   ├── helpers/                       # PropertiesReaderHelper, RegistrationHelper, JsonReaderHelper
│   │   ├── steps/definition/              # Cucumber glue (BaseSteps, CommonSteps, Hooks, LoginSteps, TasksSteps, NegativeSteps, HealthSteps)
│   │   └── test/TestRunner.java           # JUnit Platform @Suite runner executing Cucumber
│   └── resources/
│       ├── features/                      # Gherkin specifications (TasksManagement.feature, NegativeTests.feature)
│       ├── openapi.json                   # Target service OpenAPI 3.1.0 specification (git-ignored)
│       ├── test-application.properties    # Default URLs, ports, endpoint routes, and single_register flag
│       ├── test-application.local.properties # Local developer secrets such as api-key (git-ignored)
│       └── logback-test.xml               # SLF4J / Logback logging configuration
├── pom.xml                                # Maven build and Failsafe / Serenity plugin configuration
└── README.md                              # Project documentation
```

---

## 3. Standard Agent Workflow

1. **Understand the Context**:
   - Read [`.agent/rules/core_rules.md`](./rules/core_rules.md) and [`.agentic/.context/project_context.md`](../.agentic/.context/project_context.md).
   - Check repository state with `git status` and `git diff` when starting or finishing work.
2. **Design Against the API Contract**:
   - Inspect `src/test/resources/openapi.json` to verify paths, schemas, required fields, security headers (`X-API-Key`, `HTTPBearer`), and expected HTTP status codes.
3. **Implement Following Framework Patterns**:
   - Apply the patterns documented in [`.agent/skills/api_sdet.md`](./skills/api_sdet.md).
   - Keep HTTP request execution in `CommonSteps` (`@Step`), step definitions in `org.aut.steps.definition` extending `BaseSteps`, and payload/response models in `org.aut.dto`.
   - Maintain the `single_register` feature lifecycle across `Hooks`, `Constants`, and `LoginSteps`.
4. **Verify & Document**:
   - Compile with `mvn test-compile` and execute tests via `mvn clean verify` (or tag-filtered runs).
   - Keep [`.agentic/.context/session_state.md`](../.agentic/.context/session_state.md), [`.agentic/.context/project_context.md`](../.agentic/.context/project_context.md), and [`README.md`](../README.md) aligned with structural changes.
