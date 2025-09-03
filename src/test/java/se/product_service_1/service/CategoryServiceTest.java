package se.product_service_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.product_service_1.exception.CategoryAlreadyExistsException;
import se.product_service_1.exception.CategoryNotEmptyException;
import se.product_service_1.exception.CategoryNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.repository.CategoryRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    @Test
    void getCategoryByName_ShouldReturnCategory_WhenCategoryExists() {
        // Arrange
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));

        // Act
        Category result = categoryService.getCategoryByName("Electronics");

        // Assert
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals(1L, result.getId());
        verify(categoryRepository, times(1)).findByName("Electronics");
    }

    @Test
    void getCategoryByName_ShouldThrowException_WhenCategoryDoesNotExist() {
        // Arrange
        String nonExistentCategory = "NonExistent";
        when(categoryRepository.findByName(nonExistentCategory)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.getCategoryByName(nonExistentCategory);
        });
        verify(categoryRepository, times(1)).findByName(nonExistentCategory);
    }

    @Test
    void addCategory_ShouldReturnSavedCategory_WhenCategoryDoesNotExist() {
        // Arrange
        Category newCategory = Category.builder().name("Books").build();
        when(categoryRepository.findByName("Books")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(
                Category.builder().id(2L).name("Books").build());

        // Act
        Category result = categoryService.addCategory(newCategory);

        // Assert
        assertNotNull(result);
        assertEquals("Books", result.getName());
        assertEquals(2L, result.getId());
        verify(categoryRepository, times(1)).findByName("Books");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void addCategory_ShouldThrowException_WhenCategoryAlreadyExists() {
        // Arrange
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(CategoryAlreadyExistsException.class, () -> {
            categoryService.addCategory(testCategory);
        });
        verify(categoryRepository, times(1)).findByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategoryByName_ShouldDeleteCategory_WhenCategoryExistsAndHasNoProducts() {
        // Arrange
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(testCategory));
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of());
        doNothing().when(categoryRepository).deleteByName("Electronics");

        // Act
        categoryService.deleteCategoryByName("Electronics");

        // Assert
        verify(categoryRepository, times(1)).findByName("Electronics");
        verify(productService, times(1)).getProductsByCategory("Electronics");
        verify(categoryRepository, times(1)).deleteByName("Electronics");
    }

    @Test
    void deleteCategoryByName_ShouldThrowException_WhenCategoryHasProducts() {
        // Arrange
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of(mock(se.product_service_1.model.Product.class)));

        // Act & Assert
        assertThrows(CategoryNotEmptyException.class, () -> {
            categoryService.deleteCategoryByName("Electronics");
        });
        verify(categoryRepository, times(0)).deleteByName("Electronics");
        verify(productService, times(1)).getProductsByCategory("Electronics");
    }

    @Test
    void deleteCategoryByName_ShouldThrowException_WhenCategoryDoesNotExist() {
        // Arrange
        String nonExistentCategory = "NonExistent";
        when(productService.getProductsByCategory(nonExistentCategory)).thenReturn(List.of());
        when(categoryRepository.findByName(nonExistentCategory)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CategoryNotFoundException.class, () -> {
            categoryService.deleteCategoryByName(nonExistentCategory);
        });
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Arrange
        List<Category> categories = Arrays.asList(
                testCategory,
                Category.builder().id(2L).name("Books").build()
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Books", result.get(1).getName());
        verify(categoryRepository, times(1)).findAll();
    }
}