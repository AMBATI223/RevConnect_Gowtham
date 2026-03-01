package com.revconnect.app.service;

import com.revconnect.app.entity.*;
import com.revconnect.app.repository.FollowRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.impl.NetworkServiceImpl;
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
public class NetworkServiceImplTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private com.revconnect.app.repository.ConnectionRepository connectionRepository;

    private NetworkServiceImpl networkService;

    private User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return user;
    }

    @BeforeEach
    void setUp() {
        networkService = new NetworkServiceImpl(followRepository, userRepository,
                notificationService, connectionRepository);
    }

    @Test
    void followUser_NewFollow_SavesFollow() {
        User follower = createUser(1L, "alice");
        User following = createUser(2L, "bob");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.empty());
        when(followRepository.save(any(Follow.class))).thenAnswer(i -> {
            Follow f = i.getArgument(0);
            f.setId(1L);
            return f;
        });

        assertDoesNotThrow(() -> networkService.followUser("alice", "bob"));
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void followUser_ToSelf_DoesNotSave() {
        User alice = createUser(1L, "alice");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(followRepository.findByFollowerAndFollowing(alice, alice)).thenReturn(Optional.empty());

        networkService.followUser("alice", "alice");
        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void unfollowUser_ExistingFollow_DeletesFollow() {
        User follower = createUser(1L, "alice");
        User following = createUser(2L, "bob");
        Follow follow = Follow.builder().follower(follower).following(following).build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(following));
        when(followRepository.findByFollowerAndFollowing(follower, following)).thenReturn(Optional.of(follow));

        networkService.unfollowUser("alice", "bob");
        verify(followRepository).delete(follow);
    }

}
