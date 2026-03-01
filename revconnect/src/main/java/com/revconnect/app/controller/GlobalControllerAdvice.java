package com.revconnect.app.controller;

import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.NotificationService;
import com.revconnect.app.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public GlobalControllerAdvice(NotificationService notificationService,
            MessageService messageService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("unreadNotificationCount")
    public long getUnreadNotificationCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                return notificationService.getUnreadCount(user.get());
            }
        }
        return 0;
    }

    @ModelAttribute("unreadMessageCount")
    public int getUnreadMessageCount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            return messageService.getUnreadMessageCount(username);
        }
        return 0;
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) auth.getPrincipal()).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }
}
