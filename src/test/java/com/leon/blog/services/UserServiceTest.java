package com.leon.blog.services;

import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.UserRepository;
import com.leon.blog.services.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    public UserRepository userRepository;

    @InjectMocks
    public UserServiceImpl userService;

    @Test
    public void getUserById_whenUserExists_returnUser() {
        // GIVEN
        User user = User.builder().id(UUID.randomUUID()).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // WHEN
        User foundUser = userService.getUserById(user.getId());

        // THEN
        assertEquals(foundUser.getId(), user.getId());
    }

    @Test
    public void getUserById_whenUserDoesNotExist_throwException() {
        // GIVEN
        User user = User.builder().id(UUID.randomUUID()).build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}
