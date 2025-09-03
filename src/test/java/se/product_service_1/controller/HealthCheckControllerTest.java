package se.product_service_1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HealthCheckControllerTest {

    private final HealthCheckController healthCheckController = new HealthCheckController();

    @Test
    void healthCheck_ShouldReturnOk() {
        // Act
        ResponseEntity<String> response = healthCheckController.healthCheck();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Application is running", response.getBody());
    }
}