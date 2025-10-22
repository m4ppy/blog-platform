package com.leon.blog.controllers;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/errors")
public class DummyController {

    @GetMapping("/entity-not-found")
    public void throwEntityNotFound() {
        throw new EntityNotFoundException("Entity not found");
    }

    @GetMapping("/illegal-argument")
    public void throwIllegalArgument() {
        throw new IllegalArgumentException("Bad argument");
    }

    @GetMapping("/illegal-state")
    public void throwIllegalState() {
        throw new IllegalStateException("Conflict state");
    }

    @GetMapping("/bad-credentials")
    public void throwBadCredentials() {
        throw new BadCredentialsException("Invalid credentials");
    }

    @GetMapping("/unexpected")
    public void throwUnexpected() {
        throw new RuntimeException("Unexpected error");
    }
}
