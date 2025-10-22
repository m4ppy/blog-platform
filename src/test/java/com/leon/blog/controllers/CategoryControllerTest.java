package com.leon.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.blog.domain.dtos.CategoryDto;
import com.leon.blog.domain.dtos.CreateCategoryRequest;
import com.leon.blog.domain.entities.Category;
import com.leon.blog.mappers.CategoryMapper;
import com.leon.blog.services.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CategoryMapper categoryMapper;

    @Test
    void listCategories_shouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = Category.builder().id(id).name("movie").build();
        CategoryDto categoryDto = CategoryDto.builder().id(id).name("movie").build();

        when(categoryService.listCategories()).thenReturn(List.of(category));
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].name").value(category.getName()));

        verify(categoryService).listCategories();
        verify(categoryMapper).toDto(category);
    }

    @Test
    void createCategory_shouldReturnCreated() throws Exception {
        CreateCategoryRequest createCategoryRequest = CreateCategoryRequest.builder()
                .name("movie").build();

        Category category = Category.builder().name("movie").build();
        CategoryDto categoryDto = CategoryDto.builder().name("movie").build();

        when(categoryMapper.toEntity(createCategoryRequest)).thenReturn(category);
        when(categoryService.createCategory(category)).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryDto);

        mockMvc.perform(post("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(objectMapper.writeValueAsString(createCategoryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("movie"));

        verify(categoryMapper).toEntity(createCategoryRequest);
        verify(categoryService).createCategory(category);
        verify(categoryMapper).toDto(category);
    }

    @Test
    void deleteCategory_shouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/categories/{id}", id))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(id);
    }
}
