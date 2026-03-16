Feature: Update project
    As a user, 
    if my project goal changes, 
    I would like to be able to change the description of the project to be able to get a more accurate organization.

    Background:
        Given the todo manager service is running
        When I create a project with title "US4 Project" and description "Original description"
    
    # --NORMAL FLOW--
    Scenario: Change project description
        When I update the project description to "New description"
        Then the status code should be 200
        Then the project description should be "New description"

    # --ALTERNATE FLOW--
    Scenario: Change the description to none
        When I update the project description to ""
        Then the status code should be 200
        Then the project description should be ""

    # --ERROR FLOW--
    Scenario: Change description of a non existent project
        When I update the project "999" description to "Error New description"
        Then the status code should be 404
        And the error message should contain "Invalid GUID for 999 entity project"


