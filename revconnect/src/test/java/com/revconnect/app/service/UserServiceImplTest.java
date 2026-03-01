package com.revconnect.app.service;

import com.revconnect.app.dto.RegistrationDTO;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.BusinessProfileRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, businessProfileRepository, passwordEncoder);
    }

    // ---- registerUser tests ----

    @Test
    void registerUser_ValidUser_SavesSuccessfully() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setRole("USER");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> userService.registerUser(dto));
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("existinguser");
        dto.setEmail("new@example.com");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("newuser");
        dto.setEmail("duplicate@example.com");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("duplicate@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerUser_BlankUsername_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("  ");
        dto.setEmail("test@example.com");
        dto.setPassword("password123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
        assertEquals("Username is required", ex.getMessage());
    }

    @Test
    void registerUser_BlankEmail_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("testuser");
        dto.setEmail("");
        dto.setPassword("password123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.registerUser(dto));
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void registerUser_CreatorRole_SavesWithCreatorRole() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("creatoruser");
        dto.setEmail("creator@example.com");
        dto.setPassword("password123");
        dto.setRole("CREATOR");
        dto.setBusinessName("My Creator Page");

        when(userRepository.existsByUsername("creatoruser")).thenReturn(false);
        when(userRepository.existsByEmail("creator@example.com")).thenReturn(false);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L); // simulate DB save
            return u;
        });
        when(businessProfileRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> userService.registerUser(dto));
        verify(businessProfileRepository).saveAndFlush(any());
    }

    // ---- findByUsername tests ----

    @Test
    void findByUsername_UserExists_ReturnsUser() {
        User mockUser = new User();
        mockUser.setUsername("alice");
        mockUser.setEmail("alice@example.com");
        mockUser.setRole(Role.USER);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(mockUser));

        User result = userService.findByUsername("alice");
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
    }

    @Test
    void findByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.findByUsername("ghost"));
        assertTrue(ex.getMessage().contains("ghost"));
    }

    @Test
    void registerUser_PasswordIsEncoded() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setUsername("secureuser");
        dto.setEmail("secure@example.com");
        dto.setPassword("plainpassword");
        dto.setRole("USER");

        when(userRepository.existsByUsername("secureuser")).thenReturn(false);
        when(userRepository.existsByEmail("secure@example.com")).thenReturn(false);

        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            // Password should be BCrypt-encoded, not plain text
            assertNotEquals("plainpassword", saved.getPassword());
            assertTrue(saved.getPassword().startsWith("$2a$") || saved.getPassword().startsWith("$2b$"));
            return saved;
        });

        userService.registerUser(dto);
    }
}
