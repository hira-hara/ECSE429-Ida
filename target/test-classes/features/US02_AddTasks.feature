Feature: Add tasks to project
    As a user, 
    I want to add related tasks to a project, 
    in order to organize my related tasks under a specific goal. 

    Background:
        Given the todo manager service is running
        And I create a project with title "US2 Project"
    
    # -- NORMAL FLOW--
    Scenario: Add a valid task to the project
        When I add a task with title "TestTask" and description "" to the project
        Then the status code should be 201
        Then the task should appear under the project

    # --ALTERNATE FLOW--
    Scenario: Add a task with title and description
        When I add a task with title "TestTask2" and description "Task 2 adding testing" to the project
        Then the status code should be 201
        Then the task should appear under the project

    # --ERROR FLOW--
    Scenario: Add a task with to a non existent project
        When I try to add a task "FailedTest" to the project "999"
        Then the status code should be 404
        And the error message should contain "Could not find parent thing for relationship projects/999/tasks"
