package com.leon.blog.repositories;

import com.leon.blog.domain.entities.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class UserRepositoryTest {

    private final UserRepository userRepository;

    @Autowired
    public UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    public void saveAndFindUser() {
        User newUser = User.builder()
                .name("Test User")
                .email("test@test.com")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("password"))
                .build();

        userRepository.save(newUser);


        User loadedUser = userRepository.findByEmail("test@test.com").orElse(null);
        assertEquals(newUser, loadedUser);
        assertEquals("Test User", loadedUser.getName());
    }

}
