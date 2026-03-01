package com.revconnect.app.controller;

import com.revconnect.app.entity.*;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NotificationController using standaloneSetup.
 * Tests controller logic in isolation, without Spring Security /
 * GlobalModelAttributes.
 */
@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;

    private User createUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return user;
    }

    // Minimal Spring Security context holder setup for @AuthenticationPrincipal
    private void setAuthentication(String username) {
        org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                username, "encoded",
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        org.springframework.web.servlet.view.InternalResourceViewResolver viewResolver = new org.springframework.web.servlet.view.InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setCustomArgumentResolvers(
                        new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void getNotifications_WithAuthenticatedUser_Returns200() throws Exception {
        setAuthentication("alice");
        User alice = createUser("alice");
        Map<NotificationType, Boolean> prefs = new HashMap<>();
        for (NotificationType type : NotificationType.values()) {
            prefs.put(type, true);
        }
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(notificationService.getUnread(alice)).thenReturn(Collections.emptyList());
        when(notificationService.getAll(alice)).thenReturn(Collections.emptyList());
        when(notificationService.getPreferences(alice)).thenReturn(prefs);

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(view().name("notifications"))
                .andExpect(model().attributeExists("unread", "all", "preferences"));
    }

    @Test
    void getNotifications_NoAuthentication_RedirectsToLogin() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        // Controller returns "redirect:/login" when userDetails is null
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void markAllAsRead_AuthenticatedUser_RedirectsToNotifications() throws Exception {
        setAuthentication("alice");
        User alice = createUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        doNothing().when(notificationService).markAllAsRead(alice);

        mockMvc.perform(post("/notifications/readAll"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void markAsRead_ById_RedirectsToNotifications() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        mockMvc.perform(post("/notifications/read/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void setPreference_ValidType_RedirectsToNotifications() throws Exception {
        setAuthentication("alice");
        User alice = createUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        doNothing().when(notificationService).setPreference(eq(alice), eq(NotificationType.LIKE), eq(false));

        mockMvc.perform(post("/notifications/preference")
                .param("type", "LIKE")
                .param("enabled", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }

    @Test
    void setPreference_InvalidType_StillRedirects() throws Exception {
        setAuthentication("alice");
        User alice = createUser("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        // Invalid enum type should be silently ignored (controller catches
        // IllegalArgumentException)
        mockMvc.perform(post("/notifications/preference")
                .param("type", "INVALID_TYPE")
                .param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));
    }
}
