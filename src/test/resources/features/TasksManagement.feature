@e2e
Feature: Client account management

  @tasks
  Scenario Outline: The client is able to register and create a task
    Given The user is registered
    When The user creates the task "<task_name>"
    Then The user can see the created task "<task_name>"
    And The user can delete the created task "<task_name>"

  Examples: Data to create tasks
  | task_name|
  |Test Task One|
  |Test Task Two|
  |Test Task Three|

#  @registration
#  Scenario Outline: Client registration
#    Given A client asks for registration
#      | email   | password   | name   | lastname   |
#      | <email> | <password> | <name> | <lastname> |
#    Then The client should be able to login with "<email>" and "<password>"
#    And The client should be able to delete his account

#    Examples: Data for client registration
#      | email             | password   | name  | lastname  |
#      | test1@yopmail.com | 123456789a | John  | Smith     |
#      | test2@yopmail.com | 123456789b | Lois  | Spence    |
#      | test3@yopmail.com | 123456789c | Clark | Lee       |
#      | test4@yopmail.com | 123456789d | Mike  | Rodriguez |