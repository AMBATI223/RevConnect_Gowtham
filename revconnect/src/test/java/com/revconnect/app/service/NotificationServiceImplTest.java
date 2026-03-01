package com.revconnect.app.service;

import com.revconnect.app.entity.Notification;
import com.revconnect.app.entity.NotificationPreference;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.NotificationPreferenceRepository;
import com.revconnect.app.repository.NotificationRepository;
import com.revconnect.app.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    private NotificationServiceImpl notificationService;

    private User createUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return user;
    }

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(notificationRepository, preferenceRepository);
    }

    @Test
    void createNotification_WhenPreferenceEnabled_SavesNotification() {
        User user = createUser("alice");
        when(preferenceRepository.findByUserAndType(user, NotificationType.LIKE)).thenReturn(Optional.empty()); // no
                                                                                                                // pref
                                                                                                                // =
                                                                                                                // default
                                                                                                                // enabled

        notificationService.createNotification(user, NotificationType.LIKE, 1L, "bob liked your post");

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createNotification_WhenPreferenceDisabled_DoesNotSave() {
        User user = createUser("alice");
        NotificationPreference pref = new NotificationPreference();
        pref.setUser(user);
        pref.setType(NotificationType.LIKE);
        pref.setEnabled(false);
        when(preferenceRepository.findByUserAndType(user, NotificationType.LIKE)).thenReturn(Optional.of(pref));

        notificationService.createNotification(user, NotificationType.LIKE, 1L, "bob liked your post");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void getUnread_ReturnsUnreadNotifications() {
        User user = createUser("alice");
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage("Test notification");
        n.setRead(false);

        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user)).thenReturn(List.of(n));

        List<Notification> result = notificationService.getUnread(user);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
    }

    @Test
    void getUnreadCount_ReturnsCorrectCount() {
        User user = createUser("alice");
        Notification n1 = new Notification();
        Notification n2 = new Notification();

        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of(n1, n2));

        long count = notificationService.getUnreadCount(user);
        assertEquals(2L, count);
    }

    @Test
    void markAsRead_ExistingNotification_SetsReadTrue() {
        Notification n = new Notification();
        n.setId(5L);
        n.setRead(false);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        notificationService.markAsRead(5L);
        assertTrue(n.isRead());
    }

    @Test
    void markAllAsRead_MarksAllNotificationsRead() {
        User user = createUser("alice");
        Notification n1 = new Notification();
        n1.setRead(false);
        Notification n2 = new Notification();
        n2.setRead(false);

        when(notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user))
                .thenReturn(List.of(n1, n2));

        notificationService.markAllAsRead(user);
        assertTrue(n1.isRead());
        assertTrue(n2.isRead());
    }

    @Test
    void getPreferences_ReturnsAllTypes() {
        User user = createUser("alice");
        when(preferenceRepository.findByUserAndType(any(), any())).thenReturn(Optional.empty());

        Map<NotificationType, Boolean> prefs = notificationService.getPreferences(user);

        // All types default to true
        for (NotificationType type : NotificationType.values()) {
            assertTrue(prefs.containsKey(type));
            assertTrue(prefs.get(type), "Default preference should be enabled for " + type);
        }
    }

    @Test
    void setPreference_NewPreference_SavesCorrectly() {
        User user = createUser("alice");
        when(preferenceRepository.findByUserAndType(user, NotificationType.COMMENT)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.setPreference(user, NotificationType.COMMENT, false);

        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void setPreference_ExistingPreference_UpdatesCorrectly() {
        User user = createUser("alice");
        NotificationPreference pref = new NotificationPreference();
        pref.setUser(user);
        pref.setType(NotificationType.SHARE);
        pref.setEnabled(true);

        when(preferenceRepository.findByUserAndType(user, NotificationType.SHARE)).thenReturn(Optional.of(pref));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.setPreference(user, NotificationType.SHARE, false);

        assertFalse(pref.isEnabled());
        verify(preferenceRepository).save(pref);
    }
}
