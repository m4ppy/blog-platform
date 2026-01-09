package com.leon.blog.services;

import com.leon.blog.domain.dtos.RegisterRequest;
import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.UserRepository;
import com.leon.blog.security.BlogUserDetails;
import com.leon.blog.security.BlogUserDetailsService;
import com.leon.blog.services.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    public UserRepository userRepository;

    @Mock
    public PasswordEncoder passwordEncoder;

    @Mock
    public BlogUserDetailsService blogUserDetailsService;

    @Mock
    public AuthenticationService authenticationService;

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

    @Test
    public void register_whenEmailAlreadyExists_throwException() {
        // GIVEN
        RegisterRequest request = RegisterRequest.builder()
                .email("leon@gmail.com")
                .name("Leon")
                .password("12345678")
                .build();

        User user = User.builder().email(request.getEmail()).build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // WHEN & THEN
        assertThrows(IllegalStateException.class, () -> userService.register(request));
    }

    @Test
    public void register_whenEverythingIsFine_throwException() {
        // GIVEN
        RegisterRequest request = RegisterRequest.builder()
                .email("leon@gmail.com")
                .name("Leon")
                .password("12345678")
                .build();

        User user = User.builder().email(request.getEmail()).build();

        UserDetails userDetails = new BlogUserDetails(user);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        when(passwordEncoder.encode(any())).thenReturn(request.getPassword());

        //when(userRepository.save(user)).then(any());

        when(blogUserDetailsService.loadUserByUsername(request.getEmail())).thenReturn(userDetails);

        // WHEN
        UserDetails userDetails1 = userService.register(request);

        // THEN
        assertNotNull(userDetails1);

    }
}
