package com.revconnect.app.service.impl;

import com.revconnect.app.repository.NotificationPreferenceRepository;
import com.revconnect.app.repository.NotificationRepository;
import com.revconnect.app.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public void createNotification(com.revconnect.app.entity.User target,
            com.revconnect.app.entity.NotificationType type,
            Long entityId, String message) {
        Optional<com.revconnect.app.entity.NotificationPreference> prefOpt = preferenceRepository
                .findByUserAndType(target, type);
        boolean enabled = prefOpt.map(com.revconnect.app.entity.NotificationPreference::isEnabled).orElse(true);
        if (!enabled) {
            return;
        }
        com.revconnect.app.entity.Notification notification = new com.revconnect.app.entity.Notification();
        notification.setUser(target);
        notification.setType(type);
        notification.setEntityId(entityId);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.revconnect.app.entity.Notification> getUnread(com.revconnect.app.entity.User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.revconnect.app.entity.Notification> getAll(com.revconnect.app.entity.User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(com.revconnect.app.entity.User user) {
        List<com.revconnect.app.entity.Notification> unread = notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(user);
        return unread == null ? 0L : (long) unread.size();
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> n.setRead(true));
    }

    @Override
    public void markAllAsRead(com.revconnect.app.entity.User user) {
        List<com.revconnect.app.entity.Notification> unread = notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(user);
        if (unread != null) {
            unread.forEach(n -> n.setRead(true));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<com.revconnect.app.entity.NotificationType, Boolean> getPreferences(
            com.revconnect.app.entity.User user) {
        Map<com.revconnect.app.entity.NotificationType, Boolean> map = new HashMap<>();
        for (com.revconnect.app.entity.NotificationType type : com.revconnect.app.entity.NotificationType.values()) {
            Optional<com.revconnect.app.entity.NotificationPreference> pref = preferenceRepository
                    .findByUserAndType(user, type);
            map.put(type, pref.map(com.revconnect.app.entity.NotificationPreference::isEnabled).orElse(true));
        }
        return map;
    }

    @Override
    public void setPreference(com.revconnect.app.entity.User user,
            com.revconnect.app.entity.NotificationType type,
            boolean enabled) {
        com.revconnect.app.entity.NotificationPreference pref = preferenceRepository.findByUserAndType(user, type)
                .orElseGet(() -> {
                    com.revconnect.app.entity.NotificationPreference np = new com.revconnect.app.entity.NotificationPreference();
                    np.setUser(user);
                    np.setType(type);
                    return np;
                });
        pref.setEnabled(enabled);
        preferenceRepository.save(pref);
    }
}
