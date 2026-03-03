package com.revconnect.app.rest;

import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private AuthenticationManager authenticationManager;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private PasswordEncoder passwordEncoder;

        @Test
        void register_ValidInput_ReturnsToken() throws Exception {
                User user = new User();
                user.setEmail("test@example.com");
                user.setUsername("testuser");

                when(userRepository.save(any(User.class))).thenReturn(user);
                when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"testuser\", \"email\":\"test@example.com\", \"password\":\"password\", \"role\":\"USER\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
        }

        @Test
        void login_ValidCredentials_ReturnsToken() throws Exception {
                User user = new User();
                user.setEmail("test@example.com");
                user.setUsername("testuser");
                user.setRole(Role.USER);

                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(new UsernamePasswordAuthenticationToken(user, null));
                when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
                when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\":\"test@example.com\", \"password\":\"password\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
        }
}
