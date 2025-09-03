package se.product_service_1.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class RestTemplateConfigTest {

    private final RestTemplateConfig restTemplateConfig = new RestTemplateConfig();

    @Test
    void restTemplate_ShouldReturnRestTemplateInstance() {
        // Act
        RestTemplate result = restTemplateConfig.restTemplate();

        // Assert
        assertNotNull(result);
        assertInstanceOf(RestTemplate.class, result);
    }
}