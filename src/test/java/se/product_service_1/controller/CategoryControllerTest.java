package se.product_service_1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.product_service_1.dto.CategoryRequest;
import se.product_service_1.dto.CategoryResponse;
import se.product_service_1.model.Category;
import se.product_service_1.service.CategoryService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(
                testCategory,
                Category.builder().id(2L).name("Books").build()
        );
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("Electronics", response.getBody().get(0).getName());
        assertEquals("Books", response.getBody().get(1).getName());
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void getCategoryByName_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        String categoryName = "Electronics";
        when(categoryService.getCategoryByName(categoryName)).thenReturn(testCategory);

        // Act
        ResponseEntity<Category> response = categoryController.getCategoryByName(categoryName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Electronics", response.getBody().getName());
        assertEquals(1L, response.getBody().getId());
        verify(categoryService, times(1)).getCategoryByName(categoryName);
    }

    @Test
    void addCategory_ShouldReturnCreatedCategory() {
        // Arrange
        CategoryRequest categoryRequest = new CategoryRequest("Books");

        Category newCategory = Category.builder()
                .id(2L)
                .name("Books")
                .build();

        when(categoryService.addCategory(any(Category.class))).thenReturn(newCategory);

        // Act
        ResponseEntity<CategoryResponse> response = categoryController.addCategory(categoryRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Books", response.getBody().getName());
        verify(categoryService, times(1)).addCategory(any(Category.class));
    }

    @Test
    void deleteCategoryByName_ShouldReturnOk_WhenCategoryDeleted() {
        // Arrange
        String categoryName = "Electronics";
        doNothing().when(categoryService).deleteCategoryByName(categoryName);

        // Act
        ResponseEntity<Category> response = categoryController.deleteCategoryByName(categoryName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryService, times(1)).deleteCategoryByName(categoryName);
    }
}