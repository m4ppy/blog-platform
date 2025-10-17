package com.leon.blog.services;

import com.leon.blog.domain.entities.Post;
import com.leon.blog.domain.entities.Tag;
import com.leon.blog.repositories.TagRepository;
import com.leon.blog.services.impl.TagServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagServiceImpl tagService;

    @Test
    public void createTags_shouldCreateNewTagsAndReturnAll() {
        // GIVEN
        Set<String> requestedNames = Set.of("Java", "Spring", "Music");

        List<Tag> existingTags = List.of(Tag.builder().name("Java").build());

        when(tagRepository.findByNameIn(requestedNames)).thenReturn(existingTags);

        List<Tag> savedTags = new ArrayList<>(List.of(
                Tag.builder().name("Spring").build(),
                Tag.builder().name("Music").build()
        ));

        when(tagRepository.saveAll(anyList())).thenReturn(savedTags);

        // WHEN
        List<Tag> result = tagService.createTags(requestedNames);

        // THEN
        assertThat(result)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Spring", "Music");

        verify(tagRepository).findByNameIn(requestedNames);
        verify(tagRepository).saveAll(anyList());

    }

    @Test
    public void getTagById_whenExists() {
        // GIVEN
        UUID id = UUID.randomUUID();

        Tag tag = Tag.builder().id(id).name("good").build();

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

        // WHEN
        Tag foundTag = tagService.getTagById(id);

        // THEN
        assertThat(foundTag.getId()).isEqualTo(id);
        assertThat(foundTag).isEqualTo(tag);
    }

    @Test
    public void getTagById_whenDoesNotExists() {
        // GIVEN
        UUID id = UUID.randomUUID();

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> tagService.getTagById(id));
    }

    @Test
    public void getTagByIds_whenAllTagsExists_returnListOfTags() {
        // GIVEN
        Tag tag1 = Tag.builder().id(UUID.randomUUID()).name("good").build();
        Tag tag2 = Tag.builder().id(UUID.randomUUID()).name("bad").build();
        Set<UUID> ids = Set.of(tag1.getId(), tag2.getId());

        when(tagRepository.findAllById(ids)).thenReturn(List.of(tag1, tag2));

        // WHEN
        List<Tag> result = tagService.getTagByIds(ids);

        // THEN
        assertThat(result.size()).isEqualTo(ids.size());
    }

    @Test
    public void getTagByIds_whenTagNotExists_throwException() {
        // GIVEN
        Set<UUID> ids = Set.of(UUID.randomUUID(), UUID.randomUUID());

        when(tagRepository.findAllById(ids)).thenReturn(anyList());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> tagService.getTagByIds(ids));
    }

    @Test
    public void deleteTag_whenTagNotFound_doNothing() {
        // GIVEN
        UUID id = UUID.randomUUID();

        when(tagRepository.findById(id)).thenReturn(Optional.empty());

        // WHEN
        tagService.deleteTag(id);

        // THEN
        verify(tagRepository).findById(id);
        verify(tagRepository, never()).deleteById(id);
    }

    @Test
    public void deleteTag_whenTagHasNoPostsAssociatedWith() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Tag tag = Tag.builder().id(id).posts(Set.of()).build();

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

        // WHEN
        tagService.deleteTag(id);

        // THEN
        verify(tagRepository).deleteById(id);
    }

    @Test
    public void deleteTag_whenTagHasPostsAssociatedWith_throwException() {
        // GIVEN
        UUID id = UUID.randomUUID();
        Post post = Post.builder().build();
        Tag tag = Tag.builder().id(id).posts(Set.of(post)).build();

        when(tagRepository.findById(id)).thenReturn(Optional.of(tag));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> tagService.deleteTag(id));
    }
}
