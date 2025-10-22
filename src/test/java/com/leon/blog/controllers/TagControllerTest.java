package com.leon.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.blog.domain.dtos.CreateTagsRequest;
import com.leon.blog.domain.dtos.TagDto;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.mappers.TagMapper;
import com.leon.blog.services.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private TagMapper tagMapper;

    @Test
    void getAllTags_shouldReturnOk() throws Exception {
        UUID tagId = UUID.randomUUID();
        Tag tag = Tag.builder().id(tagId).name("movie").build();
        TagDto tagDto = TagDto.builder().id(tagId).name("movie").build();

        when(tagService.getTags()).thenReturn(List.of(tag));
        when(tagMapper.toTagResponse(tag)).thenReturn(tagDto);

        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(tagId.toString()))
                .andExpect(jsonPath("$[0].name").value("movie"));

        verify(tagService).getTags();
        verify(tagMapper).toTagResponse(tag);
    }

    @Test
    void createTags_shouldReturnCreated() throws Exception {
        UUID tagId = UUID.randomUUID();
        UUID tagId2 = UUID.randomUUID();

        Tag tag = Tag.builder().id(tagId).name("movie").build();
        Tag tag2 = Tag.builder().id(tagId).name("anime").build();

        TagDto tagDto = TagDto.builder().id(tagId).name("movie").build();
        TagDto tagDto2 = TagDto.builder().id(tagId).name("anime").build();

        CreateTagsRequest createTagsRequest = CreateTagsRequest.builder()
                .names(Set.of("movie", "anime"))
                .build();

        when(tagService.createTags(createTagsRequest.getNames())).thenReturn(List.of(tag, tag2));
        when(tagMapper.toTagResponse(tag)).thenReturn(tagDto);
        when(tagMapper.toTagResponse(tag2)).thenReturn(tagDto2);

        mockMvc.perform(post("/api/v1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(objectMapper.writeValueAsString(createTagsRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("movie"))
                .andExpect(jsonPath("$[1].name").value("anime"));

        verify(tagService).createTags(createTagsRequest.getNames());
        verify(tagMapper).toTagResponse(tag);
        verify(tagMapper).toTagResponse(tag2);
    }

    @Test
    void deleteTag_shouldNoContent() throws Exception {
        UUID tagId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/tags/{id}", tagId))
                .andExpect(status().isNoContent());

        verify(tagService).deleteTag(tagId);
    }
}