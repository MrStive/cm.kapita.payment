Feature: Demo Creation

  @e2e
  Scenario: Successfully create a demo
    Given Assume that I am connected with a token containing create scopes
      | scope       |
      | demo:create |
    When I call create demo API with payload
      | name      |
      | Test Demo |
    Then the create response status should be 201
    And a created demo id is returned
    And I should see the created demo in database
      | name      |
      | Test Demo |
