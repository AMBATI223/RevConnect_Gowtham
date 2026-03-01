package com.revconnect.app.controller;

import com.revconnect.app.entity.User;
import com.revconnect.app.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    public GlobalModelAttributes(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount(@AuthenticationPrincipal User currentUser) {
        if (currentUser != null) {
            return notificationService.getUnreadCount(currentUser);
        }
        return 0;
    }
}
