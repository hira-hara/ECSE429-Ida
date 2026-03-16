Feature: Delete project
    As a user, 
    I want to delete the projects that are completed, 
    to keep the main workspace clean.

    Background: 
        Given the todo manager service is running
    
    # --NORMAL FLOW--
    Scenario: Delete project that is completed
        When I create a project with title "US5 Project" and description "Original description"
        And the project is marked as completed
        When I delete the project
        Then the status code should be 200
        Then the project should not appear under the projects list

    # --ALTERNATE FLOW--
    Scenario: Delete project that is not completed
        When I create a project with title "US5 Project" and description "Not completed"
        When I delete the project
        Then the status code should be 200
        Then the project should not appear under the projects list


    # --ERROR FLOW 1--
    Scenario: Double delete project that is completed
        When I create a project with title "Error Project" and description "Original description"
        And the project is marked as completed
        When I delete the project
        Then the status code should be 200
        Then the project should not appear under the projects list
        When I delete the project
        Then the status code should be 404
    
    # --ERROR FLOW 2--
    Scenario: Delete non existent project
        When I delete the project with id "999"
        Then the status code should be 404
        And the error message should contain "Could not find any instances with projects/999"


