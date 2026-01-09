package com.leon.blog.services.impl;

import com.leon.blog.domain.dtos.RegisterRequest;
import com.leon.blog.domain.entities.User;
import com.leon.blog.repositories.UserRepository;
import com.leon.blog.security.BlogUserDetailsService;
import com.leon.blog.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BlogUserDetailsService blogUserDetailsService;

    @Override
    public User getUserById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDetails register(RegisterRequest register) {
        if (userRepository.findByEmail(register.getEmail()).isPresent()) {
            throw new IllegalStateException("email already exists");
        }

        User user = User.builder()
                .email(register.getEmail())
                .name(register.getName())
                .password(passwordEncoder.encode(register.getPassword()))
                .build();

        userRepository.save(user);

        return blogUserDetailsService.loadUserByUsername(register.getEmail());
    }


}
