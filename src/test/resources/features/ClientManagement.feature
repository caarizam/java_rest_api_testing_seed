@e2e
Feature: Client account management

  @login_test
  Scenario: Client login and health checkout
    Given The client request for login with "charlie@yopmail.com" and "12gdfgf3456"
    When The client request for check health
    Then The check health service should be "up"

  @registration
  Scenario Outline: Client registration
    Given A client asks for registration
      | email   | password   | name   | lastname   |
      | <email> | <password> | <name> | <lastname> |
    Then The client should be able to login with "<email>" and "<password>"
    And The client should be able to delete his account

    Examples: Data for client registration
      | email             | password   | name  | lastname  |
      | test1@yopmail.com | 123456789a | John  | Smith     |
      | test2@yopmail.com | 123456789b | Lois  | Spence    |
      | test3@yopmail.com | 123456789c | Clark | Lee       |
      | test4@yopmail.com | 123456789d | Mike  | Rodriguez |