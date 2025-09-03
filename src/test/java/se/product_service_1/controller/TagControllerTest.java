package se.product_service_1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.product_service_1.dto.TagRequest;
import se.product_service_1.dto.TagResponse;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.service.TagService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagControllerTest {

    @Mock
    private TagService tagService;

    @InjectMocks
    private TagController tagController;

    private Tag testTag;
    private Set<Product> products;

    @BeforeEach
    void setUp() {
        products = new HashSet<>();
        products.add(Product.builder().id(1L).name("Smartphone").build());

        testTag = Tag.builder()
                .id(1L)
                .name("Premium")
                .description("High-end products")
                .products(products)
                .build();
    }

    @Test
    void getAllTags_ShouldReturnAllTags() {
        // Arrange
        List<Tag> tags = List.of(
                testTag,
                Tag.builder()
                        .id(2L)
                        .name("Sale")
                        .description("Products on sale")
                        .products(new HashSet<>())
                        .build()
        );
        when(tagService.getAllTags()).thenReturn(tags);

        // Act
        ResponseEntity<List<TagResponse>> response = tagController.getAllTags();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Premium", response.getBody().get(0).getName());
        assertEquals(1, response.getBody().get(0).getProductCount());
        assertEquals("Sale", response.getBody().get(1).getName());
        assertEquals(0, response.getBody().get(1).getProductCount());
        verify(tagService, times(1)).getAllTags();
    }

    @Test
    void getTagByName_ShouldReturnTag_WhenTagExists() {
        // Arrange
        String tagName = "Premium";
        when(tagService.getTagByName(tagName)).thenReturn(testTag);

        // Act
        ResponseEntity<TagResponse> response = tagController.getTagByName(tagName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Premium", response.getBody().getName());
        assertEquals("High-end products", response.getBody().getDescription());
        assertEquals(1, response.getBody().getProductCount());
        verify(tagService, times(1)).getTagByName(tagName);
    }

    @Test
    void createTag_ShouldReturnCreatedTag() {
        // Arrange
        TagRequest tagRequest = new TagRequest("New", "New products");

        Tag newTag = Tag.builder()
                .id(3L)
                .name("New")
                .description("New products")
                .products(new HashSet<>())
                .build();

        when(tagService.createTag(anyString(), anyString())).thenReturn(newTag);

        // Act
        ResponseEntity<TagResponse> response = tagController.createTag(tagRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New", response.getBody().getName());
        assertEquals("New products", response.getBody().getDescription());
        assertEquals(0, response.getBody().getProductCount());
        verify(tagService, times(1)).createTag("New", "New products");
    }

    @Test
    void deleteTag_ShouldReturnOk_WhenTagDeleted() {
        // Arrange
        Long tagId = 1L;
        doNothing().when(tagService).deleteTag(tagId);

        // Act
        ResponseEntity<String> response = tagController.deleteTag(tagId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Tag deleted successfully", response.getBody());
        verify(tagService, times(1)).deleteTag(tagId);
    }

    @Test
    void searchTags_ShouldReturnMatchingTags() {
        // Arrange
        String searchTerm = "prem";
        List<Tag> tags = List.of(testTag);
        when(tagService.searchTagsByName(searchTerm)).thenReturn(tags);

        // Act
        ResponseEntity<List<TagResponse>> response = tagController.searchTags(searchTerm);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Premium", response.getBody().get(0).getName());
        verify(tagService, times(1)).searchTagsByName(searchTerm);
    }
}