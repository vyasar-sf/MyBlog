package com.practical.myblog;

import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.PostResponseDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.exception.PostValidationException;
import com.practical.myblog.model.Post;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.PostRepository;
import com.practical.myblog.repository.TagRepository;
import com.practical.myblog.service.PostServiceImpl;
import com.practical.myblog.util.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /*
     * AAA pattern for unit tests:
     * 1. Arrange entities (... = new Entity())
     * 2. Mock all repository interactions (when(...).thenReturn(..., ...))
     * 3. Invoke method (service.callMethod())
     * 4. Assertions (assertNotNull, assertEquals, assertTrue, assertThrows)
     */

    @Test
    @DisplayName("Should return a list of PostResponseDTOs")
    void getAllPosts_Success() {
        Post post1 = new Post(1L, "Title1", "Text", new HashSet<>());
        Post post2 = new Post(2L, "Title2", "Text", new HashSet<>());

        when(postRepository.findAll()).thenReturn(List.of(post1, post2));

        List<PostResponseDTO> posts = postService.getAllPosts();

        assertEquals(2, posts.size());
        assertEquals("Title1", posts.get(0).getTitle());
        assertEquals("Title2", posts.get(1).getTitle());
    }

    @Test
    @DisplayName("Should return an empty list when no posts are found")
    void getAllPosts_NoPosts() {
        when(postRepository.findAll()).thenReturn(Collections.emptyList());

        List<PostResponseDTO> posts = postService.getAllPosts();

        assertTrue(posts.isEmpty());
    }

    @Test
    @DisplayName("Should return a PostResponseDTO with an existing post")
    void getPost_ExistingPost() {
        Post post = new Post(1L, "Title", "Text", new HashSet<>());
        PostResponseDTO expectedDTO = new PostResponseDTO(post.getId(), post.getTitle(), post.getText());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        PostResponseDTO resultDTO = postService.getPost(post.getId());

        assertNotNull(resultDTO);
        assertEquals(expectedDTO.getId(), resultDTO.getId());
        assertEquals(expectedDTO.getTitle(), resultDTO.getTitle());
        assertEquals(expectedDTO.getText(), resultDTO.getText());
    }

    @Test
    @DisplayName("Should throw PostValidationException for nonexistent post")
    void getPost_NonExistentPost() {
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        PostValidationException exception = assertThrows(PostValidationException.class, () -> postService.getPost(postId));

        assertEquals(ErrorMessages.POST_NOT_FOUND_WITH_ID + postId, exception.getMessage());
    }

    @Test
    @DisplayName("Should throw PostValidationException for empty title")
    void addPost_InvalidPost() {
        PostRequestDTO postRequestDTO = new PostRequestDTO();
        postRequestDTO.setTitle("");

        PostValidationException exception = assertThrows(PostValidationException.class, () -> postService.addPost(postRequestDTO));

        assertEquals(ErrorMessages.POST_TITLE_CANNOT_BE_EMPTY, exception.getMessage());
    }

    @Test
    @DisplayName("Should add a post successfully with valid data")
    void addPost_Success() {
        PostRequestDTO postRequestDTO = new PostRequestDTO();
        postRequestDTO.setTitle("Valid Title");
        postRequestDTO.setText("Valid Text");

        Post post = new Post(1L, "Valid Title", "Valid Text", new HashSet<>());

        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponseDTO responseDTO = postService.addPost(postRequestDTO);

        assertNotNull(responseDTO);
        assertEquals("Valid Title", responseDTO.getTitle());
        assertEquals("Valid Text", responseDTO.getText());
    }

    @Test
    @DisplayName("Should return set of TagResponseDTOs for a post")
    void getTagsOfPost_Success() {
        Long postId = 1L;
        Post post = new Post(postId, "Title", "Text", new HashSet<>());

        Tag tag = new Tag(1L, "Tag", new HashSet<>());
        Set<Tag> tagSet = new HashSet<>();
        tagSet.add(tag);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.tagsByPost(postId)).thenReturn(tagSet);

        Set<TagResponseDTO> tagResponseDTOs = postService.getTagsOfPost(postId);

        assertNotNull(tagResponseDTOs);
        assertEquals(1, tagResponseDTOs.size());

        TagResponseDTO responseDTO = tagResponseDTOs.iterator().next();  // Since it's a set
        assertEquals(1L, responseDTO.getId());
        assertEquals("Tag", responseDTO.getName());
    }

    @Test
    @DisplayName("Should throw PostValidationException when post not found with id")
    void getTagsOfPost_PostNotFound() {
        Long postId = 1L;
        PostValidationException exception = assertThrows(PostValidationException.class, () -> postService.getTagsOfPost(postId));

        assertEquals(ErrorMessages.POST_NOT_FOUND_WITH_ID + postId, exception.getMessage());
    }

    @Test
    @DisplayName("Should return a PostResponseDTO with added tags")
    void addTagsToPost_Success() {
        Post post = new Post(1L, "Title", "Text", new HashSet<>());
        Tag tag = new Tag(1L, "Tag", new HashSet<>());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(tagRepository.findByName(tag.getName())).thenReturn(Optional.of(tag));
        when(postRepository.existsTagForPost(post.getId(), tag.getName())).thenReturn(false);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        ResponseEntity<PostResponseDTO> response = postService.addTagsToPost(post.getId(), List.of(tag.getName()));

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Title", response.getBody().getTitle());
        assertEquals("Text", response.getBody().getText());

        assertTrue(post.getTags().contains(tag));

        assertEquals(String.valueOf(1L), response.getHeaders().getFirst("Post-ID"));
    }

    @Test
    @DisplayName("Should remove tags from pos successfully")
    void removeTagsFromPost_Success() {
        Post post = new Post(1L, "Title", "Text", new HashSet<>());
        Tag tag = new Tag(1L, "Tag", new HashSet<>());

        Set<Tag> tagSet = new HashSet<>();
        tagSet.add(tag);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.existsTagForPost(post.getId(), tag.getName())).thenReturn(false);
        when(tagRepository.findByName(tag.getName())).thenReturn(Optional.of(tag));
        when(postRepository.tagsByPost(post.getId())).thenReturn(tagSet);

        postService.removeTagsFromPost(post.getId(), List.of(tag.getName()));

        assertTrue(post.getTags().isEmpty());

        // Verify that the post was saved after removing the tag
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Should return a list of PostResponseDTOs for a given tag")
    void getAllPostsForTag_Success() {
        String tagName = "Tag";
        Post post1 = new Post(1L, "Title1", "Text1", new HashSet<>());
        Post post2 = new Post(2L, "Title2", "Text2", new HashSet<>());

        when(postRepository.findAllPostsByTagName(tagName)).thenReturn(List.of(post1, post2));

        List<PostResponseDTO> result = postService.getAllPostsForTag(tagName);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Title1", result.get(0).getTitle());
        assertEquals("Text2", result.get(1).getText());

        verify(postRepository).findAllPostsByTagName(tagName);
    }

    @Test
    @DisplayName("Should update post successfully")
    void updatePost_Success() {
        Post post = new Post(1L, "Old Title", "Old Text", new HashSet<>());

        PostRequestDTO postRequestDTO = new PostRequestDTO();
        postRequestDTO.setTitle("New Title");
        postRequestDTO.setText("New Text");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponseDTO updatedPostDTO = postService.updatePost(post.getId(), postRequestDTO);

        assertNotNull(updatedPostDTO);
        assertEquals(post.getId(), updatedPostDTO.getId());
        assertEquals("New Title", updatedPostDTO.getTitle());
        assertEquals("New Text", updatedPostDTO.getText());

        verify(postRepository).findById(post.getId());
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("Should delete post successfully")
    void deletePost_Success() {
        Long postId = 1L;

        when(postRepository.existsById(postId)).thenReturn(true);

        postService.deletePost(postId);

        verify(postRepository).existsById(postId);
        verify(postRepository).deleteById(postId);
    }
}
