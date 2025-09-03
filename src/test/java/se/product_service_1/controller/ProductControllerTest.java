package se.product_service_1.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.product_service_1.dto.*;
import se.product_service_1.model.Category;
import se.product_service_1.model.Product;
import se.product_service_1.model.Tag;
import se.product_service_1.service.CategoryService;
import se.product_service_1.service.ProductService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private Category testCategory;
    private Tag testTag;
    private ProductResponse testProductResponse;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testTag = Tag.builder()
                .id(1L)
                .name("Premium")
                .build();

        Set<Tag> tags = new HashSet<>();
        tags.add(testTag);

        testProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(tags)
                .build();

        testProductResponse = ProductResponse.builder()
                .id(1L)
                .productName("Smartphone")
                .categoryName("Electronics")
                .price(999.99)
                .stockQuantity(10)
                .tagNames(List.of("Premium"))
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        // Arrange
        List<Product> products = List.of(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.getAllProducts();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        assertEquals("Electronics", response.getBody().get(0).getCategoryName());
        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsByCategory() {
        // Arrange
        String categoryName = "Electronics";
        List<Product> products = List.of(testProduct);

        when(categoryService.getCategoryByName(categoryName)).thenReturn(testCategory);
        when(productService.getProductsByCategory(categoryName)).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.getProductsByCategory(categoryName);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        verify(categoryService, times(1)).getCategoryByName(categoryName);
        verify(productService, times(1)).getProductsByCategory(categoryName);
    }

    @Test
    void addProduct_ShouldReturnCreatedProduct_WithNoTags() {
        // Arrange
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("Laptop");
        productRequest.setCategoryName("Electronics");
        productRequest.setPrice(1499.99);
        productRequest.setStockQuantity(5);

        Product newProduct = Product.builder()
                .id(2L)
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .tags(new HashSet<>())
                .build();

        when(categoryService.getCategoryByName("Electronics")).thenReturn(testCategory);
        when(productService.addProduct(any(Product.class))).thenReturn(newProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.addProduct(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getProductName());
        assertEquals("Electronics", response.getBody().getCategoryName());
        assertEquals(1499.99, response.getBody().getPrice());
        verify(categoryService, times(1)).getCategoryByName("Electronics");
        verify(productService, times(1)).addProduct(any(Product.class));
    }

    @Test
    void addProduct_ShouldReturnCreatedProduct_WithTags() {
        // Arrange
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("Laptop");
        productRequest.setCategoryName("Electronics");
        productRequest.setPrice(1499.99);
        productRequest.setStockQuantity(5);
        productRequest.setTagNames(List.of("Premium", "New"));

        Tag newTag = Tag.builder()
                .id(2L)
                .name("New")
                .build();

        Set<Tag> tags = new HashSet<>();
        tags.add(testTag);
        tags.add(newTag);

        Product newProduct = Product.builder()
                .id(2L)
                .name("Laptop")
                .category(testCategory)
                .price(1499.99)
                .stockQuantity(5)
                .tags(tags)
                .build();

        when(categoryService.getCategoryByName("Electronics")).thenReturn(testCategory);
        when(productService.addProductWithTags(any(Product.class), anyList())).thenReturn(newProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.addProduct(productRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getProductName());
        assertEquals(2, response.getBody().getTagNames().size());
        verify(categoryService, times(1)).getCategoryByName("Electronics");
        verify(productService, times(1)).addProductWithTags(any(Product.class), anyList());
    }

    @Test
    void deleteProduct_ShouldReturnOk_WhenProductDeleted() {
        // Arrange
        ProductDeleteRequest deleteRequest = new ProductDeleteRequest("Smartphone");
        when(productService.getProductByName("Smartphone")).thenReturn(testProduct);
        doNothing().when(productService).deleteProduct(anyLong());

        // Act
        ResponseEntity<String> response = productController.deleteProduct(deleteRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Product deleted.", response.getBody());
        verify(productService, times(1)).getProductByName("Smartphone");
        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() {
        // Arrange
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.setCurrentProductName("Smartphone");
        updateRequest.setNewProductName("Smartphone Pro");
        updateRequest.setPrice(1199.99);

        Product existingProduct = testProduct;

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone Pro")
                .category(testCategory)
                .price(1199.99)
                .stockQuantity(10)
                .tags(existingProduct.getTags())
                .build();

        when(productService.getProductByName("Smartphone")).thenReturn(existingProduct);
        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.updateProduct(updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Smartphone Pro", response.getBody().getProductName());
        assertEquals(1199.99, response.getBody().getPrice());
        verify(productService, times(1)).getProductByName("Smartphone");
        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void updateStockQuantity_ShouldReturnUpdatedProduct() {
        // Arrange
        InventoryManagementRequest request = new InventoryManagementRequest();
        request.setProductName("Smartphone");
        request.setInventoryChange(5);

        Product existingProduct = testProduct;

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(15) // 10 + 5
                .tags(existingProduct.getTags())
                .build();

        when(productService.getProductByName("Smartphone")).thenReturn(existingProduct);
        when(productService.updateProduct(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.updateStockQuantity(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(15, response.getBody().getStockQuantity());
        verify(productService, times(1)).getProductByName("Smartphone");
        verify(productService, times(1)).updateProduct(any(Product.class));
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        // Arrange
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCategoryName("Electronics");
        searchRequest.setTagNames(List.of("Premium"));

        List<Product> products = List.of(testProduct);
        when(productService.searchProducts(any(ProductSearchRequest.class))).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.searchProducts(searchRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        verify(productService, times(1)).searchProducts(any(ProductSearchRequest.class));
    }

    @Test
    void searchProductsByTags_ShouldReturnMatchingProducts() {
        // Arrange
        List<String> tags = List.of("Premium");
        List<Product> products = List.of(testProduct);
        when(productService.searchProductsByTags(tags)).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.searchProductsByTags(tags);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        verify(productService, times(1)).searchProductsByTags(tags);
    }

    @Test
    void searchProductsByAllTags_ShouldReturnMatchingProducts() {
        // Arrange
        List<String> tags = List.of("Premium");
        List<Product> products = List.of(testProduct);
        when(productService.searchProductsByAllTags(tags)).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.searchProductsByAllTags(tags);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        verify(productService, times(1)).searchProductsByAllTags(tags);
    }

    @Test
    void searchProductsByTagPattern_ShouldReturnMatchingProducts() {
        // Arrange
        String pattern = "Prem";
        List<Product> products = List.of(testProduct);
        when(productService.searchProductsByTagPattern(pattern)).thenReturn(products);

        // Act
        ResponseEntity<List<ProductResponse>> response = productController.searchProductsByTagPattern(pattern);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Smartphone", response.getBody().get(0).getProductName());
        verify(productService, times(1)).searchProductsByTagPattern(pattern);
    }

    @Test
    void addTagsToProduct_ShouldReturnUpdatedProduct() {
        // Arrange
        Long productId = 1L;
        List<String> tagNames = List.of("New");

        Tag newTag = Tag.builder()
                .id(2L)
                .name("New")
                .build();

        Set<Tag> updatedTags = new HashSet<>();
        updatedTags.add(testTag);
        updatedTags.add(newTag);

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(updatedTags)
                .build();

        when(productService.addTagsToProduct(productId, tagNames)).thenReturn(updatedProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.addTagsToProduct(productId, tagNames);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getTagNames().size());
        verify(productService, times(1)).addTagsToProduct(productId, tagNames);
    }

    @Test
    void removeTagsFromProduct_ShouldReturnUpdatedProduct() {
        // Arrange
        Long productId = 1L;
        List<String> tagNames = List.of("Premium");

        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .category(testCategory)
                .price(999.99)
                .stockQuantity(10)
                .tags(new HashSet<>())
                .build();

        when(productService.removeTagsFromProduct(productId, tagNames)).thenReturn(updatedProduct);

        // Act
        ResponseEntity<ProductResponse> response = productController.removeTagsFromProduct(productId, tagNames);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getTagNames().isEmpty());
        verify(productService, times(1)).removeTagsFromProduct(productId, tagNames);
    }
}