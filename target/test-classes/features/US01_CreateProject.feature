Feature: Create Project
    As a user,
    I want to create a new project with a title and description,
    So that I can organize my workflow with goals.

# --NORMAL FLOW--
Scenario Outline: Create a project with valid data
    Given the todo manager service is running
    When I create a project with title "User Story 1" and description "Test project for US01"
    Then the status code should be 201
    And the project title should be "User Story 1"


# --ERROR FLOW--
 # Scenario: Create a project with an invalid method (Example of Error Flow)
  #  Given the todo manager service is running
   # When I request a project with ID "nonexistent"
    #Then the status code should be 404
