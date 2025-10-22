package com.leon.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.blog.domain.dtos.AuthResponse;
import com.leon.blog.domain.dtos.LoginRequest;
import com.leon.blog.domain.entities.User;
import com.leon.blog.security.BlogUserDetails;
import com.leon.blog.services.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void login_shouldReturnTokenAndStatusOk() throws Exception {
        User user = User.builder()
                .email("test@test.com")
                .password("12345678")
                .build();

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@test.com")
                .password("12345678")
                .build();

        UserDetails userDetails = new BlogUserDetails(user);
        String tokenValue = "BPAQZ9BVVCEHG824ASFA145WWF8aFZXC0";

        when(authenticationService.authenticate(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenReturn(userDetails);
        when(authenticationService.generateToken(userDetails)).thenReturn(tokenValue);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(tokenValue))
                .andExpect(jsonPath("$.expiresIn").value(86400));

        verify(authenticationService).authenticate("test@test.com", "12345678");
        verify(authenticationService).generateToken(userDetails);
    }
}
