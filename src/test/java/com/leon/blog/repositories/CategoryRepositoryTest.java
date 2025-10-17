package com.leon.blog.repositories;

import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.entities.Category;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.domain.entities.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class CategoryRepositoryTest {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final EntityManager entityManager;

    @Autowired
    public CategoryRepositoryTest(CategoryRepository categoryRepository, UserRepository userRepository, PostRepository postRepository, TagRepository tagRepository, EntityManager entityManager) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
    }

    @Test
    public void saveAndFindCategories() {

        Category pythonCategory = categoryRepository.save(Category.builder().name("Python").build());
        Category javaCategory = categoryRepository.save(Category.builder().name("Java").build());

        List<Category> foundCategories = categoryRepository.findAll();
        Category category1 = foundCategories.getFirst();
        Category category2 = foundCategories.getLast();

        assertEquals(pythonCategory.getId(), category1.getId());
        assertEquals(pythonCategory.getName(), category1.getName());
        assertEquals(pythonCategory.getPosts(), category1.getPosts());

        assertEquals(javaCategory.getId(), category2.getId());
        assertEquals(javaCategory.getName(), category2.getName());
        assertEquals(javaCategory.getPosts(), category2.getPosts());


    }

    @Test
    public void findAllWithPostCount_fetchesPostsWithoutLazyLoading() {

        Category pythonCategory = categoryRepository.save(Category.builder().name("Python").build());
        Category javaCategory = categoryRepository.save(Category.builder().name("Java").build());
        Category springCategory = categoryRepository.save(Category.builder().name("Spring").build());

        User author = userRepository.save(User.builder()
                .name("User")
                .email("test@test.com")
                .password("pass")
                .build());

        Tag javaTag = tagRepository.save(Tag.builder().name("Java").build());

        Post post1 = postRepository.save(Post.builder()
                .title("Post 1")
                .content("Python Post")
                .status(PostStatus.PUBLISHED)
                .readingTime(2)
                .author(author)
                .category(pythonCategory)
                .tags(Set.of(javaTag))
                .build());

        Post post2 = postRepository.save(Post.builder()
                .title("Post 2")
                .content("Java Post")
                .status(PostStatus.PUBLISHED)
                .readingTime(3)
                .author(author)
                .category(javaCategory)
                .tags(Set.of(javaTag))
                .build());

        entityManager.flush();
        entityManager.clear();

        List<Category> foundCategories = categoryRepository.findAllWithPostCount();
        Category foundCategory1 = foundCategories.getFirst();
        Category foundCategory2 = foundCategories.get(1);
        Category foundCategory3 = foundCategories.getLast();

        assertEquals(foundCategory1, pythonCategory);
        assertEquals(foundCategory2, javaCategory);
        assertEquals(foundCategory3, springCategory);

        assertEquals(1, foundCategory1.getPosts().size());
        assertEquals(1, foundCategory2.getPosts().size());
        assertEquals(0, foundCategory3.getPosts().size());

    }

}
