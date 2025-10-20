package com.leon.blog.services;

import com.leon.blog.services.impl.AuthenticationServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        // Set a fake secret key for testing
        ReflectionTestUtils.setField(authenticationService, "secretKey", "12345678901234567890123456789012");
    }

    @Test
    public void authenticate_shouldReturnUserDetails() {
        // GIVEN
        String email = "test@test.com";
        String password = "password";

        UserDetails mockUser = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password(password)
                .authorities("ROLE_USER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(mockUser);

        // WHEN
        UserDetails result = authenticationService.authenticate(email, password);

        // THEN
        assertNotNull(result);
        assertEquals(email, result.getUsername());
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1))
                .loadUserByUsername(email);

    }

    @Test
    public void authenticate_whenCredentialsAreNotValid_shouldThrowException() {
        // GIVEN
        String email = "test@test.com";
        String password = "password";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // WHEN & THEN
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(email, password);
        });

        verify(userDetailsService, never()).loadUserByUsername(anyString());

    }

    @Test
    public void generateToken_whenUserDetailsIsFine_returnToken() {
        // GIVEN
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@test.com");

        // WHEN
        String token = authenticationService.generateToken(userDetails);

        // THEN
        assertNotNull(token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey("12345678901234567890123456789012".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("user@test.com", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void validateToken_shouldExtractUsernameAndReturnUserDetails() {
        // GIVEN
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);

        // Generate a real token using the service
        String token = authenticationService.generateToken(userDetails);

        // WHEN
        UserDetails result = authenticationService.validateToken(token);

        // THEN
        verify(userDetailsService).loadUserByUsername("user@test.com");
        assertEquals(userDetails, result);
    }
}