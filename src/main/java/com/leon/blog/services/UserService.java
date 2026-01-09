package com.leon.blog.services;

import com.leon.blog.domain.dtos.RegisterRequest;
import com.leon.blog.domain.entities.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface UserService {
    User getUserById(UUID id);
    UserDetails register(RegisterRequest register);
}
