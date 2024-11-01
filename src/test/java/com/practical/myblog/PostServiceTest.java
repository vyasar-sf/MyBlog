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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EntityManager entityManager;

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
    @DisplayName("Should return a page of PostResponseDTOs")
    void getAllPosts_Success() {
        Post post1 = new Post(1L, "Title1", "Text1", new HashSet<>(), "url1", "url1");
        Post post2 = new Post(2L, "Title2", "Text2", new HashSet<>(), "url2", "url2");

        PostResponseDTO responseDto1 = new PostResponseDTO(1L, "Title1", "Text1", "url1", "url1");
        PostResponseDTO responseDto2 = new PostResponseDTO(2L, "Title2", "Text2", "url2", "url2");

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), pageRequest, 2);

        when(postRepository.findAll(pageRequest)).thenReturn(postPage);
        when(modelMapper.map(post1, PostResponseDTO.class)).thenReturn(responseDto1);
        when(modelMapper.map(post2, PostResponseDTO.class)).thenReturn(responseDto2);

        Page<PostResponseDTO> posts = postService.getAllPosts(0, 10);

        assertEquals(2, posts.getContent().size());
        assertEquals("Title1", posts.getContent().get(0).getTitle());
        assertEquals("Title2", posts.getContent().get(1).getTitle());
    }

    @Test
    @DisplayName("Should return an empty page when no posts are found")
    void getAllPosts_NoPosts() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Post> emptyPostPage = new PageImpl<>(Collections.emptyList(), pageRequest, 0);

        when(postRepository.findAll(pageRequest)).thenReturn(emptyPostPage);

        Page<PostResponseDTO> posts = postService.getAllPosts(0, 10);

        assertTrue(posts.isEmpty());
    }


    @Test
    @DisplayName("Should return a PostResponseDTO with an existing post")
    void getPost_ExistingPost() {
        Post post = new Post(1L, "Title", "Text", new HashSet<>(), "url", "url");
        PostResponseDTO expectedDTO = new PostResponseDTO(post.getId(), post.getTitle(), post.getText(), post.getImageUrl(), post.getVideoUrl());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(expectedDTO);

        PostResponseDTO resultDTO = postService.getPost(post.getId());

        assertNotNull(resultDTO);
        assertEquals(expectedDTO.getId(), resultDTO.getId());
        assertEquals(expectedDTO.getTitle(), resultDTO.getTitle());
        assertEquals(expectedDTO.getText(), resultDTO.getText());
        assertEquals(expectedDTO.getImageUrl(), resultDTO.getImageUrl());
        assertEquals(expectedDTO.getVideoUrl(), resultDTO.getVideoUrl());
    }

    @Test
    @DisplayName("Should throw PostValidationException when post does not exist")
    void getPost_PostNotFound() {
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostValidationException exception = assertThrows(PostValidationException.class, () -> postService.getPost(postId));
        assertEquals("Post not found with id: " + postId, exception.getMessage());
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

        Post post = new Post(1L, "Valid Title", "Valid Text", new HashSet<>(), "url", "url");
        PostResponseDTO expectedResponseDTO = new PostResponseDTO(1L, "Valid Title", "Valid Text", "url", "url");

        when(modelMapper.map(postRequestDTO, Post.class)).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(post, PostResponseDTO.class)).thenReturn(expectedResponseDTO);

        PostResponseDTO responseDTO = postService.addPost(postRequestDTO);

        assertNotNull(responseDTO);
        assertEquals("Valid Title", responseDTO.getTitle());
        assertEquals("Valid Text", responseDTO.getText());
        assertEquals("url", responseDTO.getImageUrl());
        assertEquals("url", responseDTO.getVideoUrl());
    }

    @Test
    @DisplayName("Should return set of TagResponseDTOs for a post")
    void getTagsOfPost_Success() {
        Long postId = 1L;
        Post post = new Post(postId, "Title", "Text", new HashSet<>(), "url", "url");

        Tag tag = new Tag(1L, "Tag", new HashSet<>());
        Set<Tag> tagSet = new HashSet<>();
        tagSet.add(tag);

        TagResponseDTO expectedTagResponseDTO = new TagResponseDTO(1L, "Tag");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.tagsByPost(postId)).thenReturn(tagSet);
        when(modelMapper.map(tag, TagResponseDTO.class)).thenReturn(expectedTagResponseDTO);

        Set<TagResponseDTO> tagResponseDTOs = postService.getTagsOfPost(postId);

        assertNotNull(tagResponseDTOs);
        assertEquals(1, tagResponseDTOs.size());

        TagResponseDTO responseDTO = tagResponseDTOs.iterator().next();
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
        Post post = new Post(1L, "Title", "Text", new HashSet<>(), "url", "url");
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
        Post post = new Post(1L, "Title", "Text", new HashSet<>(), "url", "url");
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
    @DisplayName("Should return a page of PostResponseDTOs for a given tag")
    void getAllPostsForTag_Success() {
        String tagName = "Tag";
        Post post1 = new Post(1L, "Title1", "Text1", new HashSet<>(), "url", "url");
        Post post2 = new Post(2L, "Title2", "Text2", new HashSet<>(), "url", "url");

        PostResponseDTO postResponseDTO1 = new PostResponseDTO(1L, "Title1", "Text1", "url", "url");
        PostResponseDTO postResponseDTO2 = new PostResponseDTO(2L, "Title2", "Text2", "url", "url");

        PageRequest pageRequest = PageRequest.of(0, 10);
        var postPage = new PageImpl<>(List.of(post1, post2), pageRequest, 2);

        when(postRepository.findAllPostsByTagName(tagName, pageRequest)).thenReturn(Optional.of(postPage));
        when(modelMapper.map(post1, PostResponseDTO.class)).thenReturn(postResponseDTO1);
        when(modelMapper.map(post2, PostResponseDTO.class)).thenReturn(postResponseDTO2);

        Page<PostResponseDTO> result = postService.getAllPostsForTag(tagName, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        var resultPost1 = result.getContent().get(0);
        var resultPost2 = result.getContent().get(1);

        assertEquals("Title1", resultPost1.getTitle());
        assertEquals("Text1", resultPost1.getText());
        assertEquals("Title2", resultPost2.getTitle());
        assertEquals("Text2", resultPost2.getText());

        verify(postRepository).findAllPostsByTagName(tagName, pageRequest);
    }


    @Test
    @DisplayName("Should update post successfully")
    void updatePost_Success() {
        Post post = new Post(1L, "Old Title", "Old Text", new HashSet<>(), "url", "url");

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