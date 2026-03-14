package PartB.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;

import PartA.TestProjectJSONDoc;

public class StepDefinitions {

    private Response response;
    private String projectId;
    private String taskId;
    private List<String> createdProjectIds = new ArrayList<>();
    private final String BASE_URL = "http://localhost:4567";

    TestProjectJSONDoc partA = new TestProjectJSONDoc();

    @After
    public void tearDown() {
        // Manage state by deleting projects created
        for (String id : createdProjectIds) {
            given().delete(BASE_URL + "/projects/" + id);
        }
        createdProjectIds.clear();
    }

    // --GIVEN KEYWORDS--
    // Common for all Story Tests, check if service is running
    @Given("the todo manager service is running")
    public void check_service_running() {
        RestAssured.baseURI = BASE_URL;
        try {
            given().get("/projects").then().statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("Service is not running at " + BASE_URL + ". Skipping tests.");
        }
    }

    @Given("A project with id {string} exists")
    public void check_projects_exits(String projectId) {
        this.projectId = projectId;
        try {
            given()
                    .get(BASE_URL + "/projects/" + projectId)
                    .then()
                    .statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("No project " + projectId + " exits because " + e);
        }
    }

    // --AND KEYWORDS--
    @And("A task with id {string} exits")
    public void check_task_id_exists(String taskId) {
        this.taskId = taskId;
        try {
            given()
                    .get(BASE_URL + "/todos/" + this.taskId)
                    .then()
                    .statusCode(200);
        } catch (Exception e) {
            Assumptions.abort("No task " + this.taskId + " exits because " + e);
        }
    }

    @And("The task with id {string} is marked as completed")
    public void check_task_completed(String taskId) {
        this.taskId = taskId;
        try {
            given()
                    .get(BASE_URL + "/todos/" + this.taskId)
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("todos[0].doneStatus", equalTo("true"));
        } catch (Exception e) {
            Assumptions.abort("Problem with task because " + e);
        }
    }

    @And("I set the completed status of project to {string}")
    public void set_completed(String status) {
        try {
            String body = String.format("{\"completed\":\"%s\"}", status);
            this.response = given().contentType(ContentType.JSON).body(body)
                    .put(BASE_URL + "/projects/" + this.projectId);
        } catch (Exception e) {
            Assumptions.abort("Problem with amending completed because " + e);
        }
    }

    @And("the error message should contain {string}")
    public void verify_error_message(String expectedMsg) {
        this.response.then().body("errorMessages[0]", containsString(expectedMsg));
    }

    // --WHEN KEYWORDS--
    // USER STORY 1: Create Project
    @When("I create a project with title {string} and description {string}")
    public void create_project(String title, String description) {
        this.response = partA.createProject(title, false, description);
        this.projectId = this.response.jsonPath().getString("id"); // Capture id for immediate cleanup
        System.out.println("Captued ID: " + this.projectId);
        createdProjectIds.add(this.projectId);
    }

    // USER STORY 2: Add Task to Project
    @When("I add a task with title {string} to the project with id {string}")
    public void add_task_to_project(String taskId) {
        // Requirement: POST /projects/:id/task
        this.taskId = taskId;
        response = partA.linkTaskToProject(this.projectId, this.taskId);
    }

    // USER STORY 3: Delete Task from Project
    @When("I delete the task {string} from the project with id {string}")
    public void delete_task_from_project(String taskId) {
        // Requirement: DELETE /projects/:id/tasks/:id
        this.taskId = taskId;
        this.response = given().when().delete(BASE_URL + "/projects" + this.projectId + "/tasks/" + this.taskId);
    }

    // USER STORY 4: Update Description
    @When("I update the project description to {string}")
    public void update_description(String newDesc) {
        // Requirement: PUT /projects/:id
        String body = String.format("{\"description\":\"%s\"}", newDesc);
        this.response = given().contentType(ContentType.JSON).body(body).put(BASE_URL + "/projects" + this.projectId);
    }

    // USER STORY 5: Delete Project
    @When("I delete the project")
    public void delete_project() {
        // Requirement: DELETE /projects/:id
        this.response = given().delete(BASE_URL + "/projects" + this.projectId);
        // Remove from cleanup list
        createdProjectIds.remove(this.projectId);
    }

    // --THEN KEYWORDS--
    // Validations, Then steps
    @Then("the status code should be {int}")
    public void verify_status_code(int code) {
        this.response.then().log().ifValidationFails().statusCode(code);
    }

    @Then("the project description should be {string}")
    public void verify_description(String expectedDesc) {
        given().get(BASE_URL + "/projects/" + this.projectId)
                .then().body("projects[0].description", equalTo(expectedDesc));
    }

    @Then("the project title should be {string}")
    public void verify_title(String expectedTitle) {
        given().header("Accept", "application/json").when().get(BASE_URL + "/projects/" + this.projectId)
                .then().log().all().body("projects[0].title", equalTo(expectedTitle));
    }

    @Then("the task should appear under the project")
    public void verify_task_added() {
        given().get(BASE_URL + "/projects" + this.projectId + "/tasks")
                .then().body("todos.id", hasItem(taskId));
    }

    @Then("the task should not appear under the project")
    public void verify_task_gone() {
        given().get(BASE_URL + "/projects" + this.projectId + "/tasks")
                .then().body("todos.id", not(hasItem(taskId)));
    }
}
