package se.product_service_1.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void customOpenAPI_ShouldReturnCorrectlyConfiguredOpenAPI() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(result);

        // Verify Info configuration
        Info info = result.getInfo();
        assertNotNull(info);
        assertEquals("API Documentation", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("API documentation for User_Service_1 API", info.getDescription());

        // Verify security configuration
        assertNotNull(result.getSecurity());
        assertEquals(1, result.getSecurity().size());
        SecurityRequirement securityRequirement = result.getSecurity().get(0);
        assertTrue(securityRequirement.containsKey("bearerAuth"));

        // Verify security schemes
        assertNotNull(result.getComponents());
        assertNotNull(result.getComponents().getSecuritySchemes());
        assertTrue(result.getComponents().getSecuritySchemes().containsKey("bearerAuth"));

        SecurityScheme securityScheme = result.getComponents().getSecuritySchemes().get("bearerAuth");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
}