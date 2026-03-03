package com.revconnect.app.rest;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.security.JwtUtil;
import com.revconnect.app.service.MessageService;
import com.revconnect.app.service.NotificationService;
import com.revconnect.app.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostRestController.class)
@ActiveProfiles("test")
public class PostRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private MessageService messageService;

    @Test
    @WithMockUser(username = "testuser")
    void getPersonalizedFeed_AuthenticatedUser_ReturnsFeed() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setRole(Role.USER);

        Post post = new Post();
        post.setId(1L);
        post.setContent("Test post");
        post.setAuthor(author);

        when(postService.getPersonalizedFeed(anyString(), any(), any())).thenReturn(List.of(post));

        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test post"))
                .andExpect(jsonPath("$[0].author.username").value("author"));
    }

    @Test
    void getPersonalizedFeed_UnauthenticatedUser_Returns401() throws Exception {
        mockMvc.perform(get("/api/posts/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPostById_ExistingPost_ReturnsPost() throws Exception {
        User author = new User();
        author.setUsername("author");
        author.setRole(Role.USER);

        Post post = new Post();
        post.setId(10L);
        post.setContent("Specific post");
        post.setAuthor(author);

        when(postService.getPostById(10L)).thenReturn(post);

        mockMvc.perform(get("/api/posts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.content").value("Specific post"));
    }
}
