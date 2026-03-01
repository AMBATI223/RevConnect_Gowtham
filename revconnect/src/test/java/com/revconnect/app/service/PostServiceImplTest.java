package com.revconnect.app.service;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.*;
import com.revconnect.app.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private ShareRepository shareRepository;
    @Mock
    private SavedPostRepository savedPostRepository;
    @Mock
    private PostViewStatsRepository postViewStatsRepository;

    private PostServiceImpl postService;

    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return user;
    }

    private Post createPost(Long id, User author) {
        Post post = new Post();
        post.setId(id);
        post.setContent("Test post content");
        post.setAuthor(author);
        post.setPublished(true);
        return post;
    }

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(
                postRepository, userRepository, fileStorageService,
                commentRepository, likeRepository, shareRepository,
                savedPostRepository, postViewStatsRepository);
    }

    @Test
    void createPost_ValidInput_SavesPost() {
        User author = createUser(1L, "alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(
                () -> postService.createPost("alice", "Hello world!", "#test", null, null, null, null, false, false));

        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_UnknownUser_ThrowsException() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                postService.createPost("nobody", "Hello", null, null, null, null, null, false, false));

        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_ByOwner_DeletesSuccessfully() {
        User owner = createUser(1L, "bob");
        Post post = createPost(10L, owner);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> postService.deletePost(10L, "bob"));

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_ByNonOwner_ThrowsException() {
        User owner = createUser(1L, "bob");
        Post post = createPost(10L, owner);
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> postService.deletePost(10L, "charlie"));
        assertTrue(ex.getMessage().contains("Unauthorized"));
        verify(postRepository, never()).delete(any());
    }

    @Test
    void deletePost_PostNotFound_ThrowsException() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> postService.deletePost(99L, "alice"));
    }

    @Test
    void getPostById_ExistingPost_ReturnsPost() {
        User author = createUser(1L, "alice");
        Post post = createPost(5L, author);
        when(postRepository.findById(5L)).thenReturn(Optional.of(post));

        Post result = postService.getPostById(5L);
        assertNotNull(result);
        assertEquals(5L, result.getId());
    }

    @Test
    void getPostById_NotFound_ThrowsException() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> postService.getPostById(999L));
    }

    @Test
    void updatePost_ByOwner_UpdatesSuccessfully() {
        User owner = createUser(1L, "alice");
        Post post = createPost(1L, owner);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> postService.updatePost(1L, "alice", "Updated content", "#updated", null, null, null,
                false, false));
        assertEquals("Updated content", post.getContent());
    }

    @Test
    void updatePost_ByNonOwner_ThrowsException() {
        User owner = createUser(1L, "alice");
        Post post = createPost(1L, owner);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        assertThrows(RuntimeException.class,
                () -> postService.updatePost(1L, "bob", "Hacked content", null, null, null, null, false, false));
        verify(postRepository, never()).save(any());
    }

    @Test
    void togglePin_ByOwner_TogglesPin() {
        User owner = createUser(1L, "alice");
        Post post = createPost(1L, owner);
        assertFalse(post.isPinned());
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(i -> i.getArgument(0));

        postService.togglePin(1L, "alice");
        assertTrue(post.isPinned());
    }
}
