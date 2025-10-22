package com.leon.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.blog.mappers.PostMapper;
import com.leon.blog.services.AuthenticationService;
import com.leon.blog.services.PostService;
import com.leon.blog.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ErrorController.class, DummyController.class})
@AutoConfigureMockMvc(addFilters = false)
public class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnNotFound_whenEntityNotFoundExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/errors/entity-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Entity not found"));
    }

    @Test
    void shouldReturnBadRequest_whenIllegalArgumentExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/errors/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad argument"));
    }

    @Test
    void shouldReturnConflict_whenIllegalStateExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/errors/illegal-state"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Conflict state"));
    }

    @Test
    void shouldReturnUnauthorized_whenBadCredentialsExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/errors/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Incorrect username or password"));
    }

    @Test
    void shouldReturnInternalServerError_whenUnexpectedExceptionThrown() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
