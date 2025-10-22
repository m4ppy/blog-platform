package com.leon.blog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.dtos.*;
import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.CategoryRepository;
import com.leon.blog.repositories.PostRepository;
import com.leon.blog.repositories.TagRepository;
import com.leon.blog.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "application.security.jwt.secret-key=01234567890123456789012345678901"
})
@Transactional
public class BlogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create user
        User user = User.builder()
                .email("leon@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .name("leon")
                .build();
        userRepository.save(user);

        // Login to get JWT
        LoginRequest loginRequest = new LoginRequest("leon@gmail.com", "1234");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        token = JsonPath.parse(result.getResponse().getContentAsString()).read("$.token");
    }

    private RequestPostProcessor bearerToken() {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }

    private UUID createCategory(String name) throws Exception {
        CreateCategoryRequest categoryRequest = new CreateCategoryRequest(name);

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn();

        return UUID.fromString(JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", String.class));
    }

    private UUID createTag(String tagName) throws Exception {
        CreateTagsRequest tagsRequest = new CreateTagsRequest(Set.of(tagName));

        MvcResult result = mockMvc.perform(post("/api/v1/tags")
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value(tagName))
                .andReturn();

        return UUID.fromString(JsonPath.parse(result.getResponse().getContentAsString()).read("$[0].id", String.class));
    }

    private UUID createPost(String title, String content, UUID categoryId, UUID tagId) throws Exception {
        CreatePostRequestDto postRequest = CreatePostRequestDto.builder()
                .title(title)
                .content(content)
                .categoryId(categoryId)
                .tagIds(Set.of(tagId))
                .status(PostStatus.PUBLISHED)
                .build();

        MvcResult result = mockMvc.perform(post("/api/v1/posts")
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title))
                .andReturn();

        return UUID.fromString(JsonPath.parse(result.getResponse().getContentAsString()).read("$.id", String.class));
    }

    @Test
    void endToEnd_postLifecycle_success() throws Exception {
        // 1. Create category & tag
        UUID categoryId = createCategory("Tech");
        UUID tagId = createTag("movie");

        // 2. Create post
        UUID postId = createPost("Integration Test Post", "End-to-end content validation.", categoryId, tagId);

        // 3. Retrieve post
        mockMvc.perform(get("/api/v1/posts/" + postId).with(bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Test Post"))
                .andExpect(jsonPath("$.content").value("End-to-end content validation."))
                .andExpect(jsonPath("$.category.name").value("Tech"))
                .andExpect(jsonPath("$.tags[0].name").value("movie"))
                .andExpect(jsonPath("$.author.name").value("leon"));

        // 4. Update post
        UpdatePostRequestDto updateRequest = UpdatePostRequestDto.builder()
                .id(postId)
                .title("Updated Post")
                .content("Updated content here!")
                .categoryId(categoryId)
                .status(PostStatus.PUBLISHED)
                .build();

        mockMvc.perform(put("/api/v1/posts/" + postId)
                        .with(bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Post"))
                .andExpect(jsonPath("$.content").value("Updated content here!"));


        // 5. Delete post
        mockMvc.perform(delete("/api/v1/posts/" + postId).with(bearerToken()))
                .andExpect(status().isNoContent());

        // 6. Verify deleted
        mockMvc.perform(get("/api/v1/posts/" + postId).with(bearerToken()))
                .andExpect(status().isNotFound());
    }
}
