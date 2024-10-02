package com.practical.myblog;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.exception.TagValidationException;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.TagRepository;
import com.practical.myblog.service.TagServiceImpl;
import com.practical.myblog.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simulate the behavior of dependencies
 * Test the logic of the class
 */
public class TagServiceTest {
    @Mock
    private TagRepository tagRepository;

    // Service is being tested so TagRepository mock is injected to it
    @InjectMocks
    private TagServiceImpl tagService;

    // Executed before each test method
    @BeforeEach
    void setUp() {
        // Initialize mocks annotated with @Mock
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return a list of TagResponseDTOs")
    void getAllTags() {
        Tag tag1 = new Tag(1L, "Tag1", new HashSet<>());
        Tag tag2 = new Tag(2L, "Tag2", new HashSet<>());

        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        List<TagResponseDTO> tags = tagService.getAllTags();

        assertEquals(2, tags.size());
        assertEquals("Tag1", tags.get(0).getName());
        assertEquals("Tag2", tags.get(1).getName());
    }

    @Test
    @DisplayName("Should return a TagResponseDTO with an existing tag")
    void getTag_ExistingTag() {
        Tag tag = new Tag(1L, "Tag", new HashSet<>());
        TagResponseDTO expectedDTO = new TagResponseDTO(tag.getId(), tag.getName());
        when(tagRepository.findById(tag.getId())).thenReturn(Optional.of(tag));
        TagResponseDTO resultDTO = tagService.getTag(tag.getId());

        assertNotNull(resultDTO);
        assertEquals(expectedDTO.getId(), resultDTO.getId());
        assertEquals(expectedDTO.getName(), resultDTO.getName());
    }

    @Test
    @DisplayName("Should throw TagValidationException for nonexistent tag")
    void getTag_NonExistentTag() {
        Long tagId = 1L;
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());
        TagValidationException exception = assertThrows(TagValidationException.class, () -> tagService.getTag(tagId));

        assertEquals(ErrorMessages.TAG_NOT_FOUND_WITH_ID + tagId, exception.getMessage());
    }

    @Test
    @DisplayName("Should add a tag successfully with valid data")
    void addTag_SuccessForSingleTag() {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of("Valid tag name"));

        Tag tag = new Tag(1L, "Valid tag name", new HashSet<>());

        // Any instance of the Tag class is acceptable as an argument for the mocked method
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        List<TagResponseDTO> responseDTO = tagService.addTag(tagRequestDTO);

        assertNotNull(responseDTO);
        assertEquals("Valid tag name", responseDTO.get(0).getName());
    }

    @Test
    @DisplayName("Should add a tag successfully with valid data")
    void addTag_SuccessForMultipleTags() {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of("Tag1", "Tag2"));

        Tag tag1 = new Tag(1L, "Tag1", new HashSet<>());
        Tag tag2 = new Tag(1L, "Tag2", new HashSet<>());

        when(tagRepository.save(any(Tag.class))).thenReturn(tag1, tag2);

        List<TagResponseDTO> responseDTO = tagService.addTag(tagRequestDTO);

        assertNotNull(responseDTO);
        assertEquals("Tag1", responseDTO.get(0).getName());
        assertEquals("Tag2", responseDTO.get(1).getName());
    }

    @Test
    @DisplayName("Should throw TagValidationException for invalid tag")
    void addTag_InvalidTag() {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of(""));

        TagValidationException exception = assertThrows(TagValidationException.class, () -> tagService.addTag(tagRequestDTO));

        assertEquals(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TagValidationException when faced with duplicate tag")
    void addTag_DuplicateTag() {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of("Tag"));

        Tag tag = new Tag(1L, "Tag", new HashSet<>());

        // Mocks that the case where tag already exists
        when(tagRepository.findByName("Tag")).thenReturn(Optional.of(tag));

        TagValidationException exception = assertThrows(TagValidationException.class, () -> tagService.addTag(tagRequestDTO));

        assertEquals(ErrorMessages.TAG_NOT_UNIQUE, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw TagValidationException for more than one tags")
    void updateTag_MoreThanOneTag() {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of("Tag1", "Tag2"));

        TagValidationException exception = assertThrows(TagValidationException.class, () -> tagService.updateTagName(1L, tagRequestDTO));

        assertEquals(ErrorMessages.ONE_TAG_TO_UPDATE, exception.getMessage());
    }

    @Test
    @DisplayName("Should delete tag successfully when tag exists")
    void deleteTag_Success() {
        Long tagId = 1L;

        // Mock existing tag
        when(tagRepository.existsById(tagId)).thenReturn(true);

        tagService.deleteTag(tagId);

        // Verify that if the method deleteById called once during the test
        // "verify" is useful when method returns void
        verify(tagRepository, times(1)).deleteById(tagId);
    }

    @Test
    @DisplayName("Should throw TagValidationException when tag does not exist")
    void deleteTag_TagNotFound() {
        Long tagId = -1L; // Non-existing tag ID

        // Mock non-existing tag
        when(tagRepository.existsById(tagId)).thenReturn(false);

        TagValidationException exception = assertThrows(TagValidationException.class, () -> tagService.deleteTag(tagId));

        assertEquals(ErrorMessages.TAG_NOT_FOUND_WITH_ID + tagId, exception.getMessage());
    }

}
