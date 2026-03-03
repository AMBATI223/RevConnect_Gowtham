package com.revconnect.app.repository;

import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ExistingUser_ReturnsUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_ExistingUser_ReturnsUser() {
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("test2@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        entityManager.persist(user);
        entityManager.flush();

        Optional<User> found = userRepository.findByEmail("test2@example.com");
        assertTrue(found.isPresent());
        assertEquals("testuser2", found.get().getUsername());
    }

    @Test
    void findByUsername_NonExistentUser_ReturnsEmpty() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }
}
