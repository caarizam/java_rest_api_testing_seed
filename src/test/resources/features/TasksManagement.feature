@e2e
Feature: Task Management

  Background: The user is registered and logged in
    Given The user is registered

  @tasks
  Scenario Outline: The client is able to register and create a task
    When The user creates the task "<task_name>"
    Then The user can see the created task "<task_name>"
    And The user can delete the created task "<task_name>"

  Examples: Data to create tasks
  | task_name|
  |Test Task One|
  |Test Task Two|
  |Test Task Three|
    