Feature: Delete Task 
    As a customer, 
    I want to be able to delete the tasks of a project when I have once I have completed those tasks, 
    to keep my workspace clean.

    Background:
        Given the todo manager service is running
        And I create a project with title "US3 Project"


    # --NORMAL FLOW--
    Scenario: Delete done task from project
        When I add a task with title "US3 Task" and description "" to the project
        And the task is marked as completed
        When I delete the task from the project
        Then the status code should be 200
        Then the task should not appear under the project

    # --ALTERNATE FLOW--
    Scenario: Delete task from project and globally
        When I add a task with title "US3 Task" and description "" to the project
        And the task is marked as completed
        When I delete the task from the project
        Then the status code should be 200
        Then the task should not appear under the project
        And I delete the task
        Then the status code should be 200
        
    # --ERROR FLOW--
    Scenario: Double delete task from project 
        When I add a task with title "US3 Task" and description "" to the project
        And the task is marked as completed
        When I delete the task from the project
        Then the status code should be 200
        Then the task should not appear under the project
        When I delete the task from the project
        Then the status code should be 404
