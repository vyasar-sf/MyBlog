package com.practical.myblog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest // Sets up Application Context
@AutoConfigureMockMvc // Performs HTTP requests in tests without starting a real server
public class TagControllerIntegrationTest {

    // Define and start a MySQLContainer for the test environment
    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Converts Java objects to JSON and vice versa

    @Autowired
    private TagRepository tagRepository;

    private Tag tag1;
    private Tag tag2;

    // Dynamically register the MySQL properties for the Spring Boot context
    @DynamicPropertySource
    static void registerMySQLProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @BeforeEach
    public void setUp() {
        tagRepository.deleteAll();

        // IDs are auto generated
        tag1 = new Tag();
        tag1.setName("Tag 1");

        tag2 = new Tag();
        tag2.setName("Tag 2");

        tagRepository.save(tag1);
        tagRepository.save(tag2);
    }

    @Test
    @DisplayName("Should return a page of tags")
    void getTags_Success() throws Exception {
        mockMvc.perform(get("/tags?pageNo=0&pageSize=10").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is(tag1.getName())))
                .andExpect(jsonPath("$.content[1].name", is(tag2.getName())));
    }

    @Test
    @DisplayName("Should return a tag by ID")
    void getTag_Success() throws Exception {
        Long tagId = tag1.getId();

        mockMvc.perform(get("/tags/{id}", tagId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(tag1.getName())))
                .andExpect(jsonPath("$.id", is(tagId.intValue())));
    }

    @Test
    @DisplayName("Should add a new tag")
    void addTag_Success() throws Exception {
        TagRequestDTO requestDTO = new TagRequestDTO();
        requestDTO.setTags(List.of("Tag"));

        // Convert the requestDTO to JSON
        String jsonRequest = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(post("/tags").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is(requestDTO.getTags().get(0))));
    }

    @Test
    @DisplayName("Should update the name of an existing tag")
    void updateTagName_Success() throws Exception {
        TagRequestDTO requestDTO = new TagRequestDTO();
        requestDTO.setTags(List.of("Updated Tag Name"));

        String updatedTagJson = objectMapper.writeValueAsString(requestDTO);

        mockMvc.perform(patch("/tags/{id}", tag1.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTagJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(requestDTO.getTags().get(0))));
    }

    @Test
    @DisplayName("Should delete an existing tag")
    void deleteTag_Success() throws Exception {
        Long tagId = tag1.getId();

        mockMvc.perform(delete("/tags/{id}", tagId))
                .andExpect(status().isNoContent());
    }
}
