package PartC;

// Measuring the CPU and Memory usage during test 
// INcreasing the object counts and measuring time taken to POST, PUT and DELETE
import static io.restassured.RestAssured.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.lang.Math;

public class PerformanceTest {
    private static final String BASE_URL = "http://localhost:4567";
    private List<String> createdProjectIds = new ArrayList<>();

    @AfterEach
    private void TearDown() {
        // Manage state by deleting projects and tasks created
        for (String Projectid : createdProjectIds) {
            given().delete(BASE_URL + "/projects/" + Projectid);
        }
        createdProjectIds.clear();
    }

    @Test
    void runPerformanceExperiments() {
        int[] sampleSizes = { 1, 5, 10, 50, 80, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000, 4000,
                5000 };

        for (int size : sampleSizes) {
            System.out.println("Batch " + size + " START: " + java.time.LocalTime.now());
            setupObjects(size);

            // Measure Create
            long startCreate = System.nanoTime();
            String id = createProject("Performance Task");
            createdProjectIds.add(id);
            long endCreate = System.nanoTime();
            System.out.println("Create time: " + (endCreate - startCreate) * Math.pow(10, -6) + "ms");

            // Measure Change
            long startChange = System.nanoTime();
            updateProject(id, "Updated Task");
            long endChange = System.nanoTime();
            System.out.println("Update time: " + (endChange - startChange) * Math.pow(10, -6) + "ms");

            // Measure Delete
            long startDelete = System.nanoTime();
            deleteProject(id);
            long endDelete = System.nanoTime();
            System.out.println("Delete time: " + (endDelete - startDelete) * Math.pow(10, -6) + "ms");

            TearDown();

            System.out.println("Batch " + size + " END: " + java.time.LocalTime.now());
    
            // Add a 5-second pause so you can see the "cooldown" in the Perfmon graph
            try { Thread.sleep(5000); } catch (Exception e) {}
        }
    }

    // Implementation taken directly from Part A
    private String createProject(String title, Boolean completed, String description) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("completed", completed);
        body.put("description", description);

        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(BASE_URL + "/projects")
                .jsonPath()
                .getString("id");
    }

    private void setupObjects(int count) {
        for (int i = 0; i < count; i++) {
            String createdId = createProject("Initial task " + i);
            if (i % 100 == 0) { // Every 100 objects, pause for 50ms
                try {
                    Thread.sleep(50);

                } catch (Exception e) {
                }
            }
            createdProjectIds.add(createdId);
        }
    }

    private String createProject(String title) {
        return createProject(title, false, null);
    }

    private void updateProject(String id, String newTitle) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", newTitle);
        given().contentType(ContentType.JSON).body(body).put(BASE_URL + "/projects/" + id);
    }

    private void deleteProject(String id) {
        given().delete(BASE_URL + "/projects/" + id);
    }

}
