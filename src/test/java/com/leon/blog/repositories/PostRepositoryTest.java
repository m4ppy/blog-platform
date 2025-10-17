package com.leon.blog.repositories;

import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.entities.Category;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PostRepositoryTest {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Autowired
    public PostRepositoryTest(PostRepository postRepository, CategoryRepository categoryRepository, TagRepository tagRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    @Test
    public void saveAndFindAllPost() {

        User savedUser = userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build());

        Category savedCategory = categoryRepository.save(Category.builder()
                .name("TestCategory")
                .build());

        Tag savedTag1 = tagRepository.save(Tag.builder().name("TestTag1").build());
        Tag savedTag2 = tagRepository.save(Tag.builder().name("TestTag2").build());
        Set<Tag> tagSet = Set.of(savedTag1, savedTag2);

        Post savedPost = Post.builder()
                .title("The Test Post")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser)
                .category(savedCategory)
                .tags(tagSet)
                .build();

        postRepository.save(savedPost);

        postRepository.flush();
        Post foundPost = postRepository.findById(savedPost.getId()).orElseThrow();

        assertEquals("The Test Post", foundPost.getTitle());
        assertEquals(savedPost, foundPost);

    }

    @Test
    public void findAllByAuthorAndStatus() {

        User savedUser1 = userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build());

        User savedUser2 = userRepository.save(User.builder()
                .name("Test User 2")
                .email("test2@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build());

        Category savedCategory = categoryRepository.save(Category.builder()
                .name("TestCategory")
                .build());

        Tag savedTag1 = tagRepository.save(Tag.builder().name("TestTag1").build());
        Tag savedTag2 = tagRepository.save(Tag.builder().name("TestTag2").build());
        Set<Tag> tagSet = Set.of(savedTag1, savedTag2);

        Post savedPost1 = postRepository.save(Post.builder()
                .title("The Post 1")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser1)
                .category(savedCategory)
                .tags(tagSet)
                .build());

        Post savedPost2 = postRepository.save(Post.builder()
                .title("The Post 2")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser2)
                .category(savedCategory)
                .tags(null)
                .build());

        postRepository.flush();

        List<Post> foundPost = postRepository.findAllByAuthorAndStatus(savedUser1, PostStatus.PUBLISHED);
        assertTrue(foundPost.contains(savedPost1));
        assertEquals(foundPost.getFirst().getAuthor(), savedUser1);
        assertNotEquals(foundPost.getFirst().getAuthor(), savedUser2);


    }

    @Test
    public void findAllByCategory() {

        User savedUser = userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build());

        Category savedCategory1 = categoryRepository.save(Category.builder().name("TestCategory1").build());
        Category savedCategory2 = categoryRepository.save(Category.builder().name("TestCategory2").build());

        Tag savedTag1 = tagRepository.save(Tag.builder().name("TestTag1").build());
        Tag savedTag2 = tagRepository.save(Tag.builder().name("TestTag2").build());
        Set<Tag> tagSet = Set.of(savedTag1, savedTag2);

        Post savedPost1 = postRepository.save(Post.builder()
                .title("The Post 1")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser)
                .category(savedCategory1)
                .tags(tagSet)
                .build());

        Post savedPost2 = postRepository.save(Post.builder()
                .title("The Post 2")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser)
                .category(savedCategory2)
                .tags(null)
                .build());

        postRepository.flush();
        List<Post> foundPostsWithCategory1 = postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, savedCategory1);
        assertTrue(foundPostsWithCategory1.contains(savedPost1));
        assertFalse(foundPostsWithCategory1.contains(savedPost2));

        List<Post> foundPostsWithCategory2 = postRepository.findAllByStatusAndCategory(PostStatus.PUBLISHED, savedCategory2);
        assertTrue(foundPostsWithCategory2.contains(savedPost2));
        assertFalse(foundPostsWithCategory2.contains(savedPost1));

    }

    @Test
    public void findAllByTagsContaining() {

        User savedUser = userRepository.save(User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build());

        Category savedCategory = categoryRepository.save(Category.builder()
                .name("TestCategory")
                .build());

        Tag savedTag1 = tagRepository.save(Tag.builder().name("TestTag1").build());
        Tag savedTag2 = tagRepository.save(Tag.builder().name("TestTag2").build());
        Set<Tag> tagSet = Set.of(savedTag1, savedTag2);

        Post savedPost1 = postRepository.save(Post.builder()
                .title("The Post 1")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser)
                .category(savedCategory)
                .tags(tagSet)
                .build());

        Post savedPost2 = postRepository.save(Post.builder()
                .title("The Post 2")
                .content("The post for testing.")
                .status(PostStatus.PUBLISHED)
                .readingTime(1)
                .author(savedUser)
                .category(savedCategory)
                .tags(null)
                .build());


        postRepository.flush();
        List<Post> foundPosts = postRepository.findAllByStatusAndTagsContaining(PostStatus.PUBLISHED, savedTag1);
        assertTrue(foundPosts.contains(savedPost1));
        assertFalse(foundPosts.contains(savedPost2));

    }

}
