package com.leon.blog.services;

import com.leon.blog.domain.entities.Category;
import com.leon.blog.domain.entities.Post;
import com.leon.blog.repositories.CategoryRepository;
import com.leon.blog.services.impl.CategoryServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryServiceImpl categoryService;

    @Test
    public void createCategory_whenCategoryDoesNotExist_saveAndFindCategory() {
        // GIVEN
        Category category = Category.builder().name("movie").build();

        when(categoryRepository.existsByNameIgnoreCase(category.getName())).thenReturn(false);

        when(categoryRepository.save(category)).thenReturn(category);

        // WHEN
        Category foundCategory = categoryService.createCategory(category);

        // THEN
        assertEquals(foundCategory.getId(), category.getId());
        assertEquals(foundCategory.getName(), category.getName());
    }

    @Test
    public void createCategory_whenCategoryAlreadyExists_throwException() {
        // GIVEN
        Category category = Category.builder().name("movie").build();

        when(categoryRepository.existsByNameIgnoreCase(category.getName())).thenReturn(true);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(category));
    }

    @Test
    public void getCategoryById_whenCategoryExists_returnCategory() {
        // GIVEN
        Category category = Category.builder().name("movie").build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        // WHEN
        Category foundCategory = categoryService.getCategoryById(category.getId());

        // THEN
        assertEquals(foundCategory.getId(), category.getId());
        assertEquals(foundCategory.getName(), category.getName());
    }

    @Test
    public void getCategoryById_whenCategoryDoesNotExist_throwException() {
        // GIVEN
        Category category = Category.builder().name("movie").build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(category.getId()));
    }

    @Test
    public void deleteCategory_whenCategoryHasNoPostsAssociatedWith_deleteCategory() {
        // GIVEN
        Category category = Category.builder().name("movie").posts(List.of()).build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        // WHEN
        categoryService.deleteCategory(category.getId());

        // THEN
        verify(categoryRepository).deleteById(category.getId());

    }

    @Test
    public void deleteCategory_whenCategoryHasPostsAssociatedWith_throwException() {
        // GIVEN
        Post post = Post.builder().build();
        Category category = Category.builder().name("movie").posts(List.of(post)).build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(category.getId()));

    }

    @Test
    public void deleteCategory_whenCategoryDoesNotExist_doNothing() {
        // GIVEN
        Category category = Category.builder().name("movie").build();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        // WHEN
        categoryService.deleteCategory(category.getId());

        // THEN
        verify(categoryRepository, never()).deleteById(category.getId());
    }

}
























































