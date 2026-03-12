package PartB.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

public class ProjectSteps {

    private Response response;
    private String projectId;
    private String taskId;
    private List<String> createdProjectIds = new ArrayList<>();
    private final String BASE_URL = "http://localhost:4567/projects";

    @After
    public void tearDown() {
        // Manage state by deleting projects created
        for (String id : createdProjectIds) {
            given().delete(BASE_URL + "/" + id);
        }
        createdProjectIds.clear();
    }

    // USER STORY 1: Create Project
    @When("I create a project with title {string} and description {string}")
    public void create_project(String title, String description) {
        String body = String.format("{\"title\":\"%s\", \"description\":\"%s\"}", title, description);
        response = given().contentType(ContentType.JSON).body(body).post(BASE_URL);
        
        if (response.getStatusCode() == 201) {
            projectId = response.jsonPath().getString("id");
            createdProjectIds.add(projectId);
        }
    }

    // USER STORY 2: Add Task to Project
    @When("I add a task with title {string} to the project")
    public void add_task_to_project(String taskTitle) {
        // Requirement: POST /projects/:id/tasks
        String body = String.format("{\"title\":\"%s\"}", taskTitle);
        response = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                   .post(BASE_URL + "/" + projectId + "/tasks");
        
        if (response.getStatusCode() == 201) {
            taskId = response.jsonPath().getString("id");
        }
    }

    // USER STORY 3: Delete Task from Project
    @When("I delete the task from the project")
    public void delete_task_from_project() {
        // Requirement: DELETE /projects/:id/tasks/:id
        response = given().delete(BASE_URL + "/" + projectId + "/tasks/" + taskId);
    }

    // USER STORY 4: Update Description
    @When("I update the project description to {string}")
    public void update_description(String newDesc) {
        // Requirement: PUT /projects/:id
        String body = String.format("{\"description\":\"%s\"}", newDesc);
        response = given().contentType(ContentType.JSON).body(body).put(BASE_URL + "/" + projectId);
    }

    // USER STORY 5: Delete Project
    @When("I delete the project")
    public void delete_project() {
        // Requirement: DELETE /projects/:id
        response = given().delete(BASE_URL + "/" + projectId);
        // Remove from cleanup list since it's already deleted
        createdProjectIds.remove(projectId); 
    }

    // Validations, Then steps

    @Then("the status code should be {int}")
    public void verify_status(int code) {
        response.then().statusCode(code);
    }

    @Then("the project description should be {string}")
    public void verify_description(String expectedDesc) {
        given().get(BASE_URL + "/" + projectId)
               .then().body("projects[0].description", equalTo(expectedDesc));
    }

    @Then("the task should not appear under the project")
    public void verify_task_gone() {
        given().get(BASE_URL + "/" + projectId + "/tasks")
               .then().body("todos.id", not(hasItem(taskId)));
    }

    @Then("the project should return a {int} not found")
    public void verify_404(int code) {
        given().get(BASE_URL + "/" + projectId).then().statusCode(code);
    }
}
