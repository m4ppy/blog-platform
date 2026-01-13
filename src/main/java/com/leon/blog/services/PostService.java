package com.leon.blog.services;

import com.leon.blog.domain.CreatePostRequest;
import com.leon.blog.domain.UpdatePostRequest;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.User;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

public interface PostService {
    Post getPost(UUID id);
    List<Post> getAllPosts(UUID categoryId, UUID tagId);
    List<Post> getDraftPosts(User user);
    Post createPost(User user, CreatePostRequest createPostRequest);
    Post updatePost(UUID id, User user, UpdatePostRequest updatePostRequest);
    void deletePost(UUID id, User user);
}
