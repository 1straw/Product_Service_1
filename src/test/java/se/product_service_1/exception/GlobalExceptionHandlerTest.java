package se.product_service_1.exception;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setRequestURI("/test");
        webRequest = new ServletWebRequest(servletRequest);
    }

    @Test
    void handleResourceNotFound_ShouldReturnNotFoundStatus() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleBadRequest_ShouldReturnBadRequestStatus() throws Exception {
        // Arrange
        BadRequestException ex = new BadRequestException("Bad request");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleBadRequest(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad request", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleUnauthorizedException_ShouldReturnUnauthorizedStatus() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("Unauthorized");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleUnauthorizedException(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleAllExceptions_ShouldReturnInternalServerErrorStatus() {
        // Arrange
        Exception ex = new Exception("Unexpected error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleAllExceptions(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleCategoryNotFound_ShouldReturnNotFoundStatus() {
        // Arrange
        CategoryNotFoundException ex = new CategoryNotFoundException("Category not found");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleCategoryNotFound(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatus());
        assertEquals("Category not found", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleCategoryAlreadyExists_ShouldReturnConflictStatus() {
        // Arrange
        CategoryAlreadyExistsException ex = new CategoryAlreadyExistsException("Category already exists");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleCategoryAlreadyExists(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Category already exists", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }

    @Test
    void handleCategoryNotEmpty_ShouldReturnConflictStatus() {
        // Arrange
        CategoryNotEmptyException ex = new CategoryNotEmptyException("Category is not empty");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = exceptionHandler.handleCategoryNotEmpty(ex, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Category is not empty", response.getBody().getMessage());
        assertEquals("/test", response.getBody().getPath());
    }
}