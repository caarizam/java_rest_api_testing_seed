@e2e @negative
Feature: Negative API Validation and Edge Cases

  # --- Security & Authentication Negative Tests ---
  @security
  Scenario: Register user without the API key
    When The client registers a new user without the API key
    Then The response status code should be 401

  @security
  Scenario: Register user with an invalid API key
    When The client registers a new user with an invalid API key
    Then The response status code should be 401

  @security
  Scenario: Access tasks without authentication token
    When The client requests tasks without a Bearer token
    Then The response status code should be 401

  # --- Payload & Validation Negative Tests ---
  @validation
  Scenario Outline: Register user with missing required field
    When The client attempts registration with missing "<field>"
    Then The response status code should be 422

    Examples:
      | field    |
      | username |
      | password |
      | confirm  |

  @validation
  Scenario: Login with incorrect credentials
    When The client attempts to login with invalid credentials
    Then The response status code should be 401

  # --- Task Validation & Resource Not Found Tests ---
  @tasks @validation
  Scenario: Create a task with negative points
    Given The user is registered
    When The user attempts to create a task with points -1
    Then The response status code should be 422

  @tasks @notFound
  Scenario: Retrieve a non-existent task
    Given The user is registered
    When The user requests a task with id 999999
    Then The response status code should be 404

  @tasks @notFound
  Scenario: Delete a non-existent task
    Given The user is registered
    When The user attempts to delete a task with id 999999
    Then The response status code should be 404