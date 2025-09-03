package se.product_service_1.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.product_service_1.exception.ResourceNotFoundException;
import se.product_service_1.model.Tag;
import se.product_service_1.repository.TagRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    private Tag testTag;
    private List<Tag> testTags;

    @BeforeEach
    void setUp() {
        testTag = Tag.builder()
                .id(1L)
                .name("Premium")
                .description("High-end products")
                .build();

        Tag testTag2 = Tag.builder()
                .id(2L)
                .name("Sale")
                .description("Products on sale")
                .build();

        testTags = Arrays.asList(testTag, testTag2);
    }

    @Test
    void getAllTags_ShouldReturnAllTags() {
        // Arrange
        when(tagRepository.findAll()).thenReturn(testTags);

        // Act
        List<Tag> result = tagService.getAllTags();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Premium", result.get(0).getName());
        assertEquals("Sale", result.get(1).getName());
        verify(tagRepository, times(1)).findAll();
    }

    @Test
    void getTagByName_ShouldReturnTag_WhenTagExists() {
        // Arrange
        when(tagRepository.findByName("Premium")).thenReturn(Optional.of(testTag));

        // Act
        Tag result = tagService.getTagByName("Premium");

        // Assert
        assertNotNull(result);
        assertEquals("Premium", result.getName());
        assertEquals("High-end products", result.getDescription());
        verify(tagRepository, times(1)).findByName("Premium");
    }

    @Test
    void getTagByName_ShouldThrowException_WhenTagDoesNotExist() {
        // Arrange
        String nonExistentTag = "NonExistent";
        when(tagRepository.findByName(nonExistentTag)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.getTagByName(nonExistentTag);
        });
        verify(tagRepository, times(1)).findByName(nonExistentTag);
    }

    @Test
    void createTag_ShouldReturnSavedTag_WhenTagDoesNotExist() {
        // Arrange
        Tag newTag = Tag.builder()
                .name("New")
                .description("New products")
                .build();

        Tag savedTag = Tag.builder()
                .id(3L)
                .name("New")
                .description("New products")
                .build();

        when(tagRepository.existsByName("New")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

        // Act
        Tag result = tagService.createTag("New", "New products");

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("New", result.getName());
        assertEquals("New products", result.getDescription());
        verify(tagRepository, times(1)).existsByName("New");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    void createTag_ShouldThrowException_WhenTagAlreadyExists() {
        // Arrange
        when(tagRepository.existsByName("Premium")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tagService.createTag("Premium", "High-end products");
        });
        verify(tagRepository, times(1)).existsByName("Premium");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void deleteTag_ShouldDeleteTag_WhenTagExists() {
        // Arrange
        Long tagId = 1L;
        when(tagRepository.existsById(tagId)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(tagId);

        // Act
        tagService.deleteTag(tagId);

        // Assert
        verify(tagRepository, times(1)).existsById(tagId);
        verify(tagRepository, times(1)).deleteById(tagId);
    }

    @Test
    void deleteTag_ShouldThrowException_WhenTagDoesNotExist() {
        // Arrange
        Long nonExistentTagId = 99L;
        when(tagRepository.existsById(nonExistentTagId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tagService.deleteTag(nonExistentTagId);
        });
        verify(tagRepository, times(1)).existsById(nonExistentTagId);
        verify(tagRepository, never()).deleteById(anyLong());
    }

    @Test
    void searchTagsByName_ShouldReturnMatchingTags() {
        // Arrange
        String searchTerm = "prem";
        when(tagRepository.findByNameContainingIgnoreCase(searchTerm)).thenReturn(List.of(testTag));

        // Act
        List<Tag> result = tagService.searchTagsByName(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Premium", result.get(0).getName());
        verify(tagRepository, times(1)).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    void getOrCreateTags_ShouldReturnExistingTags_WhenAllTagsExist() {
        // Arrange
        List<String> tagNames = Arrays.asList("Premium", "Sale");
        when(tagRepository.findByName("Premium")).thenReturn(Optional.of(testTag));
        when(tagRepository.findByName("Sale")).thenReturn(Optional.of(testTags.get(1)));

        // Act
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("Premium")));
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("Sale")));
        verify(tagRepository, times(1)).findByName("Premium");
        verify(tagRepository, times(1)).findByName("Sale");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void getOrCreateTags_ShouldCreateNewTags_WhenTagsDoNotExist() {
        // Arrange
        List<String> tagNames = Arrays.asList("New", "Special");

        Tag newTag = Tag.builder()
                .id(3L)
                .name("New")
                .description("Auto-skapad tagg")
                .build();

        Tag specialTag = Tag.builder()
                .id(4L)
                .name("Special")
                .description("Auto-skapad tagg")
                .build();

        when(tagRepository.findByName("New")).thenReturn(Optional.empty());
        when(tagRepository.findByName("Special")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class)))
                .thenReturn(newTag)
                .thenReturn(specialTag);

        // Act
        Set<Tag> result = tagService.getOrCreateTags(tagNames);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("New")));
        assertTrue(result.stream().anyMatch(tag -> tag.getName().equals("Special")));
        verify(tagRepository, times(1)).findByName("New");
        verify(tagRepository, times(1)).findByName("Special");
        verify(tagRepository, times(2)).save(any(Tag.class));
    }
}