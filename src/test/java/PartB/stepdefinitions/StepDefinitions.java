package PartB.stepdefinitions;

import io.cucumber.java.en.*;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.Json;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.*;
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
    private List<String> createdTasksIds = new ArrayList<>();
    private final String BASE_URL = "http://localhost:4567";

    TestProjectJSONDoc partA = new TestProjectJSONDoc();

    @Before
    public void setup() {
        this.projectId = null;
        this.taskId = null;
        this.response = null;
    }

    @After
    public void tearDown() {
        // Manage state by deleting projects and tasks created
        for (String Projectid : createdProjectIds) {
            given().delete(BASE_URL + "/projects/" + Projectid);
        }
        createdProjectIds.clear();

        for (String Taskid : createdTasksIds) {
            given().delete(BASE_URL + "/todos/" + Taskid);
        }
        createdTasksIds.clear();
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

    @And("the task is marked as completed")
    public void check_task_completed() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"doneStatus\":true}")
                .when()
                .post(BASE_URL + "/todos/" + this.taskId);
    }

    @And("I set the completed status of project to {string}")
    public void set_completed_input(String status) {
        try {
            String body = String.format("{\"completed\":\"%s\"}", status);
            this.response = given().contentType(ContentType.JSON).body(body)
                    .put(BASE_URL + "/projects/" + this.projectId);
        } catch (Exception e) {
            Assumptions.abort("Problem with amending completed because " + e);
        }
    }

    @And("the project is marked as completed")
    public void set_completed() {
        try {
            String body = String.format("{\"completed\":\"%s\"}", true);
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

    @And("I create a project with title {string}")
    public void create_project_and(String title) {
        this.response = partA.createProject(title, false, null);
        this.projectId = this.response.jsonPath().getString("id"); // Capture id for immediate cleanup

        System.out.println("Captued ID: " + this.projectId);
        createdProjectIds.add(this.projectId);
    }

    @And("I delete the task")
    public void delete_task() {
        this.response = given().delete(BASE_URL + "/todos/" + this.taskId);
        createdTasksIds.remove(this.taskId);
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
    @When("I add a task with title {string} and description {string} to the project")
    public void add_task_to_project(String title, String desc) {
        // Requirement: POST /projects/:id/task
        this.response = partA.linkTaskToProject(this.projectId, title, false, desc);
        this.taskId = this.response.jsonPath().getString("id");

        createdTasksIds.add(this.taskId);

        System.out.println("DEBUG US2: ProjectID=" + this.projectId + " TaskID=" + this.taskId);
    }

    @When("I try to add a task {string} to the project {string}")
    public void add_task_project_id(String task, String projectId) {
        this.response = partA.linkTaskToProject(projectId, task, false, null);
        this.taskId = this.response.jsonPath().getString("id");

        createdTasksIds.add(this.taskId);
    }

    // USER STORY 3: Delete Task from Project
    @When("I delete the task from the project")
    public void delete_task_from_project() {
        // Requirement: DELETE /projects/:id/tasks/:id
        this.response = given().when().delete(BASE_URL + "/projects/" + this.projectId + "/tasks/" + this.taskId);
        System.out.println("Delete Status: " + this.response.getStatusCode());
    }

    // USER STORY 4: Update Description
    @When("I update the project description to {string}")
    public void update_description(String newDesc) {
        // Requirement: PUT /projects/:id
        String body = String.format("{\"description\":\"%s\"}", newDesc);
        this.response = given().contentType(ContentType.JSON).body(body).put(BASE_URL + "/projects/" + this.projectId);
    }

    @When("I update the project {string} description to {string}")
    public void update_description_id(String projectId, String newDesc) {
        // Requirement: PUT /projects/:id
        String body = String.format("{\"description\":\"%s\"}", newDesc);
        this.response = given().contentType(ContentType.JSON).body(body).put(BASE_URL + "/projects/" + projectId);
    }

    // USER STORY 5: Delete Project
    @When("I delete the project")
    public void delete_project() {
        // Requirement: DELETE /projects/:id
        this.response = given().delete(BASE_URL + "/projects/" + this.projectId);
        // Remove from cleanup list
        createdProjectIds.remove(this.projectId);
    }

    @When("I delete the project with id {string}")
    public void delete_project_id(String projectId) {
        this.projectId = projectId;
        this.response = given().delete(BASE_URL + "/projects/" + this.projectId);
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
        // tasksof.id creates a list of just the Ids: ["1", "2"]
        given().header("Accept", "application/json").when().get(BASE_URL + "/todos/" + this.taskId)
                .then().log().ifValidationFails().body("todos[0].tasksof.id", hasItem(this.projectId));
    }

    @Then("the task should not appear under the project")
    public void verify_task_gone() {
        given().header("Accept", "application/json").when().get(BASE_URL + "/todos/" + this.taskId)
                .then().log().ifValidationFails().body("todos[0].tasksof.id", not(hasItem(this.projectId)));
    }

    @Then("the project should not appear under the projects list")
    public void verify_project_gone() {
        given().header("Accept", "application/json").when().get(BASE_URL + "/projects")
                .then().log().ifValidationFails().body("projects[0].id", not(hasItem(this.projectId)));
    }

}
