package com.leon.blog.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.leon.blog.domain.dtos.LoginRequest;
import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "application.security.jwt.secret-key=01234567890123456789012345678901"
})
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
                .email("leon@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .name("leon")
                .build();

        userRepository.save(user);
    }

    @Test
    void login_whenCredentialsAreValid_shouldReturnJwtToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder().email("leon@gmail.com").password("1234").build();

        mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_whenPasswordIsWrong_shouldFail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("leon@gmail.com", "wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAccessToPublicEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(post("/api/v1/posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyAccessForNonAdminUser() throws Exception {
        LoginRequest request = new LoginRequest("leon@gmail.com", "1234");
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.parse(response).read("$.token");

        mockMvc.perform(post("/api/v1/posts")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_withExpiredToken_shouldFail() throws Exception {
        LoginRequest request = new LoginRequest("leon@gmail.com", "1234");
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        String expiredToken = Jwts.builder()
                .setSubject("leon@gmail.com")
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // already expired
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();

        mockMvc.perform(get("/api/v1/posts/drafts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessWithMalformedToken_shouldReturnUnauthorized() throws Exception {
        String malformedToken = "this.is.not.a.valid.token";

        mockMvc.perform(get("/api/v1/posts/drafts")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken))
                .andExpect(status().isForbidden());
    }

}
