package com.leon.blog.repositories;

import com.leon.blog.domain.PostStatus;
import com.leon.blog.domain.entities.Category;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.domain.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TagRepositoryTest {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    @Autowired
    public TagRepositoryTest(TagRepository tagRepository, UserRepository userRepository, PostRepository postRepository, CategoryRepository categoryRepository, EntityManager entityManager) {
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
    }


    @Test
    public void saveAndFindTags() {
        Tag newTag1 = Tag.builder()
                .name("music")
                .build();

        Tag newTag2 = Tag.builder()
                .name("movie")
                .build();

        tagRepository.save(newTag1);
        tagRepository.save(newTag2);

        List<Tag> tagList = new ArrayList<>(tagRepository.findAll());

        assertEquals(tagList.getFirst(), newTag1);
        assertEquals(tagList.getLast(), newTag2);
        assertEquals(tagList.getFirst().getName(), newTag1.getName());

    }

    @Test
    void findAllWithPostCount_fetchesPostsWithoutLazyLoading() {
        // given
        User author = userRepository.save(User.builder()
                .name("User")
                .email("test@test.com")
                .password("pass")
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Tech")
                .build());

        Tag javaTag = tagRepository.save(Tag.builder().name("Java").build());
        Tag springTag = tagRepository.save(Tag.builder().name("Spring").build());
        Tag pythonTag = tagRepository.save(Tag.builder().name("Python").build());

        Post post1 = postRepository.save(Post.builder()
                .title("Post 1")
                .content("Java Post")
                .status(PostStatus.PUBLISHED)
                .readingTime(2)
                .author(author)
                .category(category)
                .tags(Set.of(javaTag, springTag))
                .build());

        entityManager.flush();
        entityManager.clear();

        // when
        List<Tag> tags = tagRepository.findAllWithPostCount();
        tags.forEach(tag -> Hibernate.initialize(tag.getPosts()));
        for (Tag tag : tags) {
            if(!Objects.equals(tag.getName(), "Python")) {
                assertEquals(1, tag.getPosts().size());
            } else {
                assertEquals(0, tag.getPosts().size());
            }
        }

    }
}
