package com.revconnect.app.service;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.repository.*;
import com.revconnect.app.service.impl.InteractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InteractionServiceImplTest {

    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ShareRepository shareRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostViewStatsRepository postViewStatsRepository;
    @Mock
    private NotificationService notificationService;

    private InteractionServiceImpl interactionService;

    @BeforeEach
    void setUp() {
        interactionService = new InteractionServiceImpl(
                likeRepository, commentRepository, shareRepository,
                postRepository, userRepository, postViewStatsRepository, notificationService);
    }

    private User createUser(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(Role.USER);
        return user;
    }

    private Post createPost(Long id, User author) {
        Post post = new Post();
        post.setId(id);
        post.setAuthor(author);
        return post;
    }

    @Test
    void toggleLike_NewLike_ShouldSaveLike() {
        User user = createUser("don", 1L);
        User author = createUser("author", 2L);
        Post post = createPost(10L, author);

        when(userRepository.findByUsername("don")).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(false);

        interactionService.toggleLike(10L, "don");

        verify(likeRepository).save(any());
        verify(notificationService).createNotification(eq(author), eq(NotificationType.LIKE), eq(10L), anyString());
    }

    @Test
    void toggleLike_ExistingLike_ShouldRemoveLike() {
        User user = createUser("don", 1L);
        Post post = createPost(10L, createUser("author", 2L));

        when(userRepository.findByUsername("don")).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));
        when(likeRepository.existsByPostAndUser(post, user)).thenReturn(true);

        interactionService.toggleLike(10L, "don");

        verify(likeRepository).deleteByPostAndUser(post, user);
    }

    @Test
    void addComment_ValidInput_ShouldSaveComment() {
        User user = createUser("don", 1L);
        User author = createUser("author", 2L);
        Post post = createPost(10L, author);

        when(userRepository.findByUsername("don")).thenReturn(Optional.of(user));
        when(postRepository.findById(10L)).thenReturn(Optional.of(post));

        interactionService.addComment(10L, "don", "Great post!");

        verify(commentRepository).save(any());
        verify(notificationService).createNotification(eq(author), eq(NotificationType.COMMENT), eq(10L), anyString());
    }
}
