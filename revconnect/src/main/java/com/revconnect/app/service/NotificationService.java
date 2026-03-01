package com.revconnect.app.service;

import com.revconnect.app.entity.Notification;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.User;
import java.util.List;
import java.util.Map;

public interface NotificationService {
    void createNotification(User target, NotificationType type, Long entityId, String message);

    List<Notification> getUnread(User user);

    List<Notification> getAll(User user);

    long getUnreadCount(User user);

    void markAsRead(Long notificationId);

    void markAllAsRead(User user);

    Map<NotificationType, Boolean> getPreferences(User user);

    void setPreference(User user, NotificationType type, boolean enabled);
}
