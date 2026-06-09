@e2e @user-event
Feature: User creation from event

  Scenario: Create a user after consuming a creation event
    Given I assume there is no user with the following data in the database
      | id                                   |
      | 234d9cbc-563e-42f1-8de0-31dece250fe8 |
    When An event with following data is received
      | id                                   | event_type   | channel                | name          | firstname | lastname | email                |
      | 234d9cbc-563e-42f1-8de0-31dece250fe8 | USER_CREATED | authentis.user.created | john.doe.test | John      | Doe      | john.doe@example.com |
    Then I should see that there is a user with the following data in the database
      | id                                   | name          | firstname | lastname | email                |
      | 234d9cbc-563e-42f1-8de0-31dece250fe8 | john.doe.test | John      | Doe      | john.doe@example.com |

  Scenario: Ignore a duplicated user creation event with the same event id
    Given I assume there is no user with the following data in the database
      | id                                   |
      | 16b2d676-4231-44c4-a1d4-a7e5d630639d |
    When An event with following data is received
      | event_id                             | id                                   | event_type   | channel                | name           | firstname | lastname | email                   |
      | 7ffb93e5-3592-4a42-949c-b17b0917c4b3 | 16b2d676-4231-44c4-a1d4-a7e5d630639d | USER_CREATED | authentis.user.created | duplicate.user | Jane      | Doe      | jane.duplicate@test.com |
    And An event with following data is received
      | event_id                             | id                                   | event_type   | channel                | name           | firstname | lastname | email                   |
      | 7ffb93e5-3592-4a42-949c-b17b0917c4b3 | 16b2d676-4231-44c4-a1d4-a7e5d630639d | USER_CREATED | authentis.user.created | duplicate.user | Jane      | Doe      | jane.duplicate@test.com |
    Then I should see that there is a user with the following data in the database
      | id                                   | name           | firstname | lastname | email                   |
      | 16b2d676-4231-44c4-a1d4-a7e5d630639d | duplicate.user | Jane      | Doe      | jane.duplicate@test.com |
    And I should see that there is exactly 1 user with id "16b2d676-4231-44c4-a1d4-a7e5d630639d" in the database
    And I should see that the inbox contains exactly 1 event with id "7ffb93e5-3592-4a42-949c-b17b0917c4b3"

  Scenario: Ignore a duplicated user creation when two different events target the same user id
    Given I assume there is no user with the following data in the database
      | id                                   |
      | e675f037-5188-48a8-af32-6ea6d45be660 |
    When An event with following data is received
      | event_id                             | id                                   | event_type   | channel                | name        | firstname | lastname | email                |
      | 4be0cf80-08dc-4943-b8c8-0f8e7d1ad3ca | e675f037-5188-48a8-af32-6ea6d45be660 | USER_CREATED | authentis.user.created | stable.user | Marie     | Curie    | stable.user@test.com |
    And An event with following data is received
      | event_id                             | id                                   | event_type   | channel                | name        | firstname | lastname | email                |
      | 2f51c09f-c8ce-4ea6-8515-a0c7830bf8a3 | e675f037-5188-48a8-af32-6ea6d45be660 | USER_CREATED | authentis.user.created | stable.user | Marie     | Curie    | stable.user@test.com |
    Then I should see that there is a user with the following data in the database
      | id                                   | name        | firstname | lastname | email                |
      | e675f037-5188-48a8-af32-6ea6d45be660 | stable.user | Marie     | Curie    | stable.user@test.com |
    And I should see that there is exactly 1 user with id "e675f037-5188-48a8-af32-6ea6d45be660" in the database
    And I should see that the inbox contains exactly 1 event with id "4be0cf80-08dc-4943-b8c8-0f8e7d1ad3ca"
    And I should see that the inbox contains exactly 1 event with id "2f51c09f-c8ce-4ea6-8515-a0c7830bf8a3"
