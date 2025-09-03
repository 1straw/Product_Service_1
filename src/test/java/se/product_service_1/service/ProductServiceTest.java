package se.product_service_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.product_service_1.dto.ProductSearchRequest;
import se.product_service_1.exception.ProductAlreadyExistsException;
import se.product_service_1.exception.ProductNotFoundException;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.CategoryRepository;
import se.product_service_1.repository.ProductRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;
    private Tag testTag;
    private Set<Tag> testTags;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("Premium")
                .description("High-end products")
                .build();

        testTags = new HashSet<>();
        testTags.add(testTag);

        testProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(testTags)
                .build();
    }

    @Test
    void addProduct_ShouldReturnSavedProduct_WhenProductDoesNotExist() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .build();

        when(productRepository.findByName("Laptop")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(
                Product.builder()
                        .id(2L)
                        .name("Laptop")
                        .category(testCategory)
                        .price(1499.99)
                        .stockQuantity(5)
                        .build()
        );

        // Act
        Product result = productService.addProduct(newProduct);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(1499.99, result.getPrice());
        verify(productRepository, times(1)).findByName("Laptop");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Arrange
        when(productRepository.findByName("Smartphone")).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(ProductAlreadyExistsException.class, () -> {
            productService.addProduct(testProduct);
        });
        verify(productRepository, times(1)).findByName("Smartphone");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProductWithTags_ShouldReturnSavedProduct_WhenProductDoesNotExist() {
        // Arrange
        Product newProduct = Product.builder()
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .build();

        List<String> tagNames = List.of("Premium", "New");
        Set<Tag> tags = new HashSet<>();
        tags.add(testTag);

        Tag newTag = Tag.builder()
                .id(2L)
                .name("New")
                .build();
        tags.add(newTag);

        Product savedProduct = Product.builder()
                .id(2L)
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .tags(tags)
                .build();

        when(productRepository.findByName("Laptop")).thenReturn(Optional.empty());
        when(tagService.getOrCreateTags(tagNames)).thenReturn(tags);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Act
        Product result = productService.addProductWithTags(newProduct, tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(2, result.getTags().size());
        verify(productRepository, times(1)).findByName("Laptop");
        verify(tagService, times(1)).getOrCreateTags(tagNames);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(productId);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    void getProductByName_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        when(productRepository.findByName("Smartphone")).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductByName("Smartphone");

        // Assert
        assertNotNull(result);
        assertEquals("Smartphone", result.getName());
        assertEquals(999.99, result.getPrice());
        verify(productRepository, times(1)).findByName("Smartphone");
    }

    @Test
    void getProductByName_ShouldThrowException_WhenProductDoesNotExist() {
        // Arrange
        String nonExistentProduct = "NonExistent";
        when(productRepository.findByName(nonExistentProduct)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> {
            productService.getProductByName(nonExistentProduct);
        });
        verify(productRepository, times(1)).findByName(nonExistentProduct);
    }

    @Test
    void getProductById_ShouldReturnProduct_WhenProductExists() {
        // Arrange
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Smartphone", result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getProductById_ShouldThrowException_WhenProductDoesNotExist() {
        // Arrange
        Long nonExistentProductId = 99L;
        when(productRepository.findById(nonExistentProductId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getProductById(nonExistentProductId);
        });
        verify(productRepository, times(1)).findById(nonExistentProductId);
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        Product product2 = Product.builder()
                .id(2L)
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .build();

        List<Product> products = Arrays.asList(testProduct, product2);
        when(productRepository.findAllWithTags()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        assertEquals("Laptop", result.get(1).getName());
        verify(productRepository, times(1)).findAllWithTags();
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsByCategory() {
        // Arrange
        String categoryName = "Electronics";
        List<Product> products = List.of(testProduct);
        when(productRepository.findByCategoryName(categoryName)).thenReturn(products);

        // Act
        List<Product> result = productService.getProductsByCategory(categoryName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        verify(productRepository, times(1)).findByCategoryName(categoryName);
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() {
        // Arrange
        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone 2.0")
                .category(testCategory)
                .price(1099.99)
                .stockQuantity(10)
                .tags(testTags)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.updateProduct(updatedProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Smartphone 2.0", result.getName());
        assertEquals(1099.99, result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void searchProductsByTags_ShouldReturnProductsWithTags() {
        // Arrange
        List<String> tagNames = List.of("Premium");
        List<Product> products = List.of(testProduct);
        when(productRepository.findByTagNames(tagNames)).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByTags(tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        verify(productRepository, times(1)).findByTagNames(tagNames);
    }

    @Test
    void searchProductsByAllTags_ShouldReturnProductsWithAllTags() {
        // Arrange
        List<String> tagNames = List.of("Premium");
        List<Product> products = List.of(testProduct);
        when(productRepository.findByAllTagNames(tagNames, 1)).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByAllTags(tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        verify(productRepository, times(1)).findByAllTagNames(tagNames, 1);
    }

    @Test
    void searchProductsByTagPattern_ShouldReturnProductsWithMatchingTagPattern() {
        // Arrange
        String tagPattern = "Prem";
        List<Product> products = List.of(testProduct);
        when(productRepository.findByTagNameContaining(tagPattern)).thenReturn(products);

        // Act
        List<Product> result = productService.searchProductsByTagPattern(tagPattern);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        verify(productRepository, times(1)).findByTagNameContaining(tagPattern);
    }

    @Test
    void searchProducts_ShouldReturnProductsMatchingSearchCriteria_WhenCategoryAndTagsProvided() {
        // Arrange
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCategoryName("Electronics");
        searchRequest.setTagNames(List.of("Premium"));

        List<Product> products = List.of(testProduct);
        when(productRepository.findByCategoryAndTagNames("Electronics", List.of("Premium"))).thenReturn(products);

        // Act
        List<Product> result = productService.searchProducts(searchRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Smartphone", result.get(0).getName());
        verify(productRepository, times(1)).findByCategoryAndTagNames("Electronics", List.of("Premium"));
    }

    @Test
    void addTagsToProduct_ShouldAddTagsToProduct() {
        // Arrange
        Long productId = 1L;
        List<String> tagNames = List.of("New");

        Tag newTag = Tag.builder()
                .id(2L)
                .name("New")
                .build();

        Set<Tag> newTags = new HashSet<>();
        newTags.add(newTag);

        Set<Tag> combinedTags = new HashSet<>(testTags);
        combinedTags.add(newTag);

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(combinedTags)
                .build();

        // Här är ändringen - mocka repository istället för service
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(tagService.getOrCreateTags(tagNames)).thenReturn(newTags);
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        Product result = productService.addTagsToProduct(productId, tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTags().size());
        verify(tagService, times(1)).getOrCreateTags(tagNames);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void removeTagsFromProduct_ShouldRemoveTagsFromProduct() {
        // Arrange
        Long productId = 1L;
        List<String> tagNames = List.of("Premium");

        Product productWithoutTags = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(new HashSet<>())
                .build();

        // Här är ändringen - mocka repository istället för service
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(productWithoutTags);

        // Act
        Product result = productService.removeTagsFromProduct(productId, tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTags().size());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}