package com.practical.myblog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.model.Post;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.PostRepository;
import com.practical.myblog.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
@ActiveProfiles("test")
public class PostControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects to JSON and vice versa

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;

    private Post post1;
    private Post post2;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        tagRepository.deleteAll();

        tag1 = new Tag();
        tag1.setName("Tag 1");
        tagRepository.save(tag1);

        tag2 = new Tag();
        tag2.setName("Tag 2");
        tagRepository.save(tag2);

        post1 = new Post();
        post1.setTitle("Post 1");
        post1.setTags(new HashSet<>(Set.of(tag1)));
        postRepository.save(post1);

        post2 = new Post();
        post2.setTitle("Post 2");
        post2.setTags(new HashSet<>(Set.of(tag2)));
        postRepository.save(post2);
    }

    @Test
    @DisplayName("Should return a list of posts")
    void getPosts_Success() throws Exception {
        mockMvc.perform(get("/posts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is(post1.getTitle())))
                .andExpect(jsonPath("$[1].title", is(post2.getTitle())));
    }

    @Test
    @DisplayName("Should return a post by ID")
    void getPost_Success() throws Exception {
        Long postId = post1.getId();

        mockMvc.perform(get("/posts/{id}", postId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(post1.getTitle())))
                .andExpect(jsonPath("$.id", is(postId.intValue())));
    }

    @Test
    @DisplayName("Should add a new post")
    void addPost_Success() throws Exception {
        PostRequestDTO requestDTO = new PostRequestDTO();
        requestDTO.setTitle("Title");

        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/posts").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest)) // Attach the JSON request body
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(requestDTO.getTitle())));
    }

    @Test
    @DisplayName("Should update the tile of an existing post")
    void updatePostTitle_Success() throws Exception {
        PostRequestDTO requestDTO = new PostRequestDTO();
        requestDTO.setTitle("Title");

        String updatedPostJson = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(put("/posts/{id}", post1.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPostJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(requestDTO.getTitle())));
    }

    @Test
    @DisplayName("Should delete an existing post by ID")
    void deletePost_Success() throws Exception {
        Long postId = post1.getId();

        mockMvc.perform(delete("/posts/{postId}", postId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return bad request when trying to delete a non-existent post")
    void deletePost_NotFound() throws Exception {
        Long nonExistentPostId = 999L; // An ID that doesn't exist

        mockMvc.perform(delete("/posts/{postId}", nonExistentPostId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return the tags of a post by post ID")
    void getTagsOfPost_Success() throws Exception {
        Long postId = post1.getId();

        mockMvc.perform(get("/posts/tags/{postId}", postId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(tag1.getName())));
    }

    @Test
    @DisplayName("Should add tags to an existing post")
    void addTagsToPost_Success() throws Exception {
        TagRequestDTO tagRequestDTO = new TagRequestDTO();
        tagRequestDTO.setTags(List.of("Tag 2")); // The tag to be added

        String tagRequestJson = objectMapper.writeValueAsString(tagRequestDTO);

        mockMvc.perform(post("/posts/{postId}/tags", post1.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(tagRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(post1.getId().intValue())))
                .andExpect(jsonPath("$.title", is(post1.getTitle())))
                .andExpect(jsonPath("$.text").value(post1.getText()))
                .andExpect(header().string("Post-ID", String.valueOf(post1.getId())));
    }

    @Test
    @DisplayName("Should return all posts for a given tag")
    void getAllPostsForTag_Success() throws Exception {
        String tagName = "Tag 1";
        mockMvc.perform(get("/posts/tag/{tagName}", tagName).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(post1.getTitle())));
    }

}
