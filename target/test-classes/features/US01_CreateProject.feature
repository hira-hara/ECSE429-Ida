Feature: Create Project
    As a user,
    I want to create a new project with a title and description,
    So that I can organize my workflow with goals.

    Background:
        Given the todo manager service is running

    # --NORMAL FLOW--
    Scenario: Create a project with valid data
        When I create a project with title "User Story 1" and description "Test project for US01"
        Then the status code should be 201
        And the project title should be "User Story 1"


    # --ALTERNATE FLOW 1--
    Scenario: Create a project with only title
        When I create a project with title "Only title project" and description ""
        Then the status code should be 201
        And the project title should be "Only title project"
    

    # --ALTERNATE FLOW 2--
    Scenario: Create a project with only description
        When I create a project with title "" and description "Only description here!"
        Then the status code should be 201
        And the project description should be "Only description here!"


    # --ERROR FLOW--
    Scenario: Create a project with an invalid input
        When I create a project with title "Invalid completed" and description "Testing Error"
        And I set the completed status of project to "not a boolean"
        Then the status code should be 400
        And the error message should contain "Failed Validation: completed should be BOOLEAN"
