package com.leon.blog.services;

import com.leon.blog.domain.CreatePostRequest;
import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.UpdatePostRequest;
import com.leon.blog.domain.entities.Category;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.CategoryRepository;
import com.leon.blog.repositories.PostRepository;
import com.leon.blog.repositories.TagRepository;
import com.leon.blog.services.impl.CategoryServiceImpl;
import com.leon.blog.services.impl.PostServiceImpl;
import com.leon.blog.services.impl.TagServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    public PostRepository postRepository;

    @Mock
    public CategoryServiceImpl categoryService;

    @Mock
    public TagServiceImpl tagService;

    @InjectMocks
    public PostServiceImpl postService;

    @Test
    public void createPost_whenEverythingIsFine_saveAndFindPost() {
        // GIVEN
        User user = User.builder().build();

        Category category = Category.builder().name("movie").build();

        CreatePostRequest createPostRequest = CreatePostRequest.builder()
                .title("the Movie")
                .content("this is post about movie.")
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of())
                .build();

        when(categoryService.getCategoryById(category.getId())).thenReturn(category);

        when(tagService.getTagByIds(Set.of())).thenReturn(List.of());

        // WHEN
        Post post = postService.createPost(user, createPostRequest);

        // THEN
        verify(postRepository).save(any());
    }

    @Test
    public void getPost_whenPostExists_returnPost() {
        // GIVEN
        Post post = Post.builder().build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // WHEN
        Post foundPost = postService.getPost(post.getId());

        // THEN
        assertEquals(foundPost.getId(), post.getId());
    }

    @Test
    public void getPost_whenPostDoesNotExist_throwException() {
        // GIVEN
        UUID id = UUID.randomUUID();

        when(postRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> postService.getPost(id));
    }

    @Test
    public void getAllPosts_whenFindByCategoryAndTag_returnPosts() {
        // GIVEN
        Category category1 = Category.builder().id(UUID.randomUUID()).build();
        Category category2 = Category.builder().build();

        Tag tag = Tag.builder().id(UUID.randomUUID()).build();

        Post post1 = Post.builder().status(PostStatus.PUBLISHED).category(category1).tags(Set.of(tag)).build();
        Post post2 = Post.builder().category(category1).tags(Set.of()).build();
        Post post3 = Post.builder().category(category2).tags(Set.of(tag)).build();
        Post post4 = Post.builder().category(category2).tags(Set.of()).build();

        when(categoryService.getCategoryById(category1.getId())).thenReturn(category1);

        when(tagService.getTagById(tag.getId())).thenReturn(tag);

        when(postRepository.findAllByStatusAndCategoryAndTagsContaining(PostStatus.PUBLISHED, category1, tag))
                .thenReturn(List.of(post1));

        // WHEN
        List<Post> foundPost = postService.getAllPosts(category1.getId(), tag.getId());

        // THEN
        assertEquals(1, foundPost.size());
        assertEquals(foundPost.getFirst().getId(), post1.getId());
    }

    @Test
    public void getAllPosts_whenFindByCategory_returnPostsAssociatedWithCategory() {
        // GIVEN
        Category category1 = Category.builder().id(UUID.randomUUID()).build();

        Post post1 = Post.builder().status(PostStatus.PUBLISHED).category(category1).tags(Set.of()).build();
        Post post2 = Post.builder().status(PostStatus.PUBLISHED).category(category1).tags(Set.of()).build();
        Post post3 = Post.builder().status(PostStatus.PUBLISHED).tags(Set.of()).build();

        when(categoryService.getCategoryById(category1.getId())).thenReturn(category1);

        when(postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, category1))
                .thenReturn(List.of(post1, post2));

        // WHEN
        List<Post> foundPost = postService.getAllPosts(category1.getId(), null);

        // THEN
        assertEquals(2, foundPost.size());
        assertEquals(foundPost.getFirst().getId(), post1.getId());
    }

    @Test
    public void getAllPosts_whenFindByTag_returnPostsAssociatedWithTag() {
        // GIVEN
        Tag tag = Tag.builder().id(UUID.randomUUID()).build();

        Post post1 = Post.builder().status(PostStatus.PUBLISHED).tags(Set.of(tag)).build();
        Post post2 = Post.builder().status(PostStatus.PUBLISHED).tags(Set.of()).build();

        when(tagService.getTagById(tag.getId())).thenReturn(tag);

        when(postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, tag))
                .thenReturn(List.of(post1));

        // WHEN
        List<Post> foundPost = postService.getAllPosts(null, tag.getId());

        // THEN
        assertEquals(1, foundPost.size());
        assertEquals(foundPost.getFirst().getId(), post1.getId());
    }

    @Test
    public void getAllPosts_whenFindWithoutAnything_returnAllPosts() {
        // GIVEN
        Post post1 = Post.builder().status(PostStatus.PUBLISHED).build();
        Post post2 = Post.builder().status(PostStatus.PUBLISHED).build();

        when(postRepository.findAllByStatus(PostStatus.PUBLISHED))
                .thenReturn(List.of(post1, post2));

        // WHEN
        List<Post> foundPost = postService.getAllPosts(null, null);

        // THEN
        assertEquals(2, foundPost.size());
        assertEquals(foundPost.getFirst().getId(), post1.getId());
    }

    @Test
    public void updatePost_whenEverythingIsFine_updateAndFindPost() {
        // GIVEN
        User user = User.builder().build();

        Category category1 = Category.builder().id(UUID.randomUUID()).name("movie").build();
        Category category2 = Category.builder().id(UUID.randomUUID()).name("anime").build();

        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("good").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("bad").build();


        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title("the anime")
                .content("this is post about anime.")
                .status(PostStatus.PUBLISHED)
                .categoryId(category2.getId())
                .tagIds(Set.of(tag1.getId()))
                .build();

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .category(category1)
                .tags(Set.of(tag1, tag2))
                .author(user)
                .status(PostStatus.PUBLISHED)
                .title("the movie")
                .content("this is post about movie")
                .readingTime(1)
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryService.getCategoryById(updatePostRequest.getCategoryId())).thenReturn(category2);

        when(tagService.getTagByIds(updatePostRequest.getTagIds())).thenReturn(List.of(tag1));

        // WHEN
        Post updatedPost = postService.updatePost(post.getId(), updatePostRequest);

        // THEN
        verify(postRepository).save(any());

        assertNotNull(updatedPost);
        assertEquals(post.getId(), updatedPost.getId());
        assertEquals("the anime", updatedPost.getTitle());
        assertEquals(category2, updatedPost.getCategory());
        assertEquals(1, updatedPost.getTags().size());
        assertEquals(Set.of(tag1), updatedPost.getTags());
    }

    @Test
    public void updatePost_whenCategoryIsSame_updateAndFindPost() {
        // GIVEN
        User user = User.builder().build();

        Category category1 = Category.builder().id(UUID.randomUUID()).name("movie").build();
        Category category2 = Category.builder().id(UUID.randomUUID()).name("anime").build();

        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("good").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("bad").build();


        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title("the anime")
                .content("this is post about anime.")
                .status(PostStatus.PUBLISHED)
                .categoryId(category1.getId())
                .tagIds(Set.of(tag1.getId()))
                .build();

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .category(category1)
                .tags(Set.of(tag1, tag2))
                .author(user)
                .status(PostStatus.PUBLISHED)
                .title("the movie")
                .content("this is post about movie")
                .readingTime(1)
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(tagService.getTagByIds(updatePostRequest.getTagIds())).thenReturn(List.of(tag1));

        // WHEN
        Post updatedPost = postService.updatePost(post.getId(), updatePostRequest);

        // THEN
        verify(postRepository).save(any());

        assertNotNull(updatedPost);
        assertEquals(post.getId(), updatedPost.getId());
        assertEquals("the anime", updatedPost.getTitle());
        assertEquals(post.getCategory(), updatedPost.getCategory());
        assertEquals(1, updatedPost.getTags().size());
        assertEquals(Set.of(tag1), updatedPost.getTags());
    }

    @Test
    public void updatePost_whenTagsAreSame_updateAndFindPost() {
        // GIVEN
        User user = User.builder().build();

        Category category1 = Category.builder().id(UUID.randomUUID()).name("movie").build();
        Category category2 = Category.builder().id(UUID.randomUUID()).name("anime").build();

        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("good").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("bad").build();


        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title("the anime")
                .content("this is post about anime.")
                .status(PostStatus.PUBLISHED)
                .categoryId(category2.getId())
                .tagIds(Set.of(tag1.getId(), tag2.getId()))
                .build();

        Post post = Post.builder()
                .id(UUID.randomUUID())
                .category(category1)
                .tags(Set.of(tag1, tag2))
                .author(user)
                .status(PostStatus.PUBLISHED)
                .title("the movie")
                .content("this is post about movie")
                .readingTime(1)
                .build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(categoryService.getCategoryById(updatePostRequest.getCategoryId())).thenReturn(category2);

        // WHEN
        Post updatedPost = postService.updatePost(post.getId(), updatePostRequest);

        // THEN
        verify(postRepository).save(any());

        assertNotNull(updatedPost);
        assertEquals(post.getId(), updatedPost.getId());
        assertEquals("the anime", updatedPost.getTitle());
        assertEquals(category2, updatedPost.getCategory());
        assertEquals(2, updatedPost.getTags().size());
        assertEquals(post.getTags(), updatedPost.getTags());
    }

    @Test
    public void updatePost_whenPostDoesNotExist_throwException() {
        // GIVEN
        Category category = Category.builder().id(UUID.randomUUID()).name("movie").build();

        UpdatePostRequest updatePostRequest = UpdatePostRequest.builder()
                .title("the anime")
                .content("this is post about anime.")
                .status(PostStatus.PUBLISHED)
                .categoryId(category.getId())
                .tagIds(Set.of())
                .build();

        Post post = Post.builder().id(UUID.randomUUID()).build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> postService.updatePost(post.getId(), updatePostRequest));
    }

    @Test
    public void deletePost_whenPostExists_deletePost() {
        // GIVEN
        Post post = Post.builder().id(UUID.randomUUID()).build();

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        // WHEN
        postService.deletePost(post.getId());

        // THEN
        verify(postRepository).delete(any(Post.class));
    }

    @Test
    public void deletePost_whenPostDoesNotExist_throwException() {
        // GIVEN
        UUID id = UUID.randomUUID();

        when(postRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> postService.deletePost(id));
    }

}