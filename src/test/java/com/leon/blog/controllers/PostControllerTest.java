package com.leon.blog.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leon.blog.domain.CreatePostRequest;
import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.UpdatePostRequest;
import com.leon.blog.domain.dtos.AuthorDto;
import com.leon.blog.domain.dtos.CreatePostRequestDto;
import com.leon.blog.domain.dtos.PostDto;
import com.leon.blog.domain.dtos.UpdatePostRequestDto;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.User;
import com.leon.blog.mappers.PostMapper;
import com.leon.blog.services.PostService;
import com.leon.blog.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private PostMapper postMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void getAllPosts_shouldReturnAllPosts() throws Exception {
        Post post = Post.builder().build();
        PostDto postDto = PostDto.builder().build();
        when(postService.getAllPosts(null, null)).thenReturn(List.of(post));
        when(postMapper.toDto(post)).thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void createPost() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();

        CreatePostRequestDto createPostRequestDto = CreatePostRequestDto.builder()
                .title("Test Title")
                .content("Test Content")
                .categoryId(UUID.randomUUID())
                .status(PostStatus.PUBLISHED)
                .build();

        CreatePostRequest createPostRequest = CreatePostRequest.builder().build();

        Post post = Post.builder().build();
        PostDto postDto = PostDto.builder().title("Test Title").build();

        when(userService.getUserById(userId)).thenReturn(user);
        when(postMapper.toCreatePostRequest(createPostRequestDto)).thenReturn(createPostRequest);
        when(postService.createPost(eq(user), eq(createPostRequest))).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(postDto);

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .requestAttr("userId", userId)
                        .content(objectMapper.writeValueAsString(createPostRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void getDrafts() throws Exception {
        User user = User.builder().id(UUID.randomUUID()).build();

        UUID postId = UUID.randomUUID();

        Post post = Post.builder()
                .id(postId)
                .title("The Movie")
                .content("The movie post")
                .status(PostStatus.DRAFT)
                .author(user)
                .build();

        PostDto postDto = PostDto.builder()
                .id(postId)
                .title("The Movie")
                .content("The movie post")
                .status(PostStatus.DRAFT)
                .build();

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(postService.getDraftPosts(user)).thenReturn(List.of(post));
        when(postMapper.toDto(any(Post.class))).thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts/drafts")
                        .requestAttr("userId", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(postId.toString()))
                .andExpect(jsonPath("$[0].title").value("The Movie"))
                .andExpect(jsonPath("$[0].content").value("The movie post"))
                .andExpect(jsonPath("$[0].status").value("DRAFT"));

        // verify interactions
        verify(userService).getUserById(user.getId());
        verify(postService).getDraftPosts(user);
        verify(postMapper).toDto(post);
    }

    @Test
    void getPost() throws Exception {
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .title("Test Title")
                .build();

        PostDto postDto = PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .build();

        when(postService.getPost(post.getId())).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(postDto);

        mockMvc.perform(get("/api/v1/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void updatePost() throws Exception {
        UUID postId = UUID.randomUUID();

        UpdatePostRequestDto updatePostRequestDto = UpdatePostRequestDto.builder()
                .id(postId)
                .title("Updated Title")
                .content("Updated Content")
                .categoryId(UUID.randomUUID())
                .status(PostStatus.PUBLISHED)
                .build();

        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title(updatePostRequestDto.getTitle())
                .content(updatePostRequestDto.getContent())
                .categoryId(updatePostRequestDto.getCategoryId())
                .status(updatePostRequestDto.getStatus())
                .build();

        Post post = Post.builder()
                .id(postId)
                .title(updatePostRequest.getTitle())
                .content(updatePostRequest.getContent())
                .status(updatePostRequest.getStatus())
                .build();

        PostDto postDto = PostDto.builder()
                .id(postId)
                .title(post.getTitle())
                .content(post.getContent())
                .status(post.getStatus())
                .build();

        when(postMapper.toUpdatePostRequest(updatePostRequestDto)).thenReturn(updatePostRequest);
        when(postService.updatePost(postId, updatePostRequest)).thenReturn(post);
        when(postMapper.toDto(post)).thenReturn(postDto);

        mockMvc.perform(put("/api/v1/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePostRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(postMapper).toUpdatePostRequest(updatePostRequestDto);
        verify(postService).updatePost(postId, updatePostRequest);
        verify(postMapper).toDto(post);
    }

    @Test
    void deletePost() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/posts/{id}", postId))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(eq(postId));
    }

}
