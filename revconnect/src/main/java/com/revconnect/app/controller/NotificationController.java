package com.revconnect.app.controller;

import com.revconnect.app.entity.Notification;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> unread = notificationService.getUnread(currentUser);
        List<Notification> all = notificationService.getAll(currentUser);
        Map<NotificationType, Boolean> preferences = notificationService.getPreferences(currentUser);
        model.addAttribute("unread", unread);
        model.addAttribute("all", all);
        model.addAttribute("preferences", preferences);
        return "notifications";
    }

    @PostMapping("/read/{id}")
    public String markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";
    }

    @PostMapping("/readAll")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        notificationService.markAllAsRead(currentUser);
        return "redirect:/notifications";
    }

    @PostMapping("/preference")
    public String setPreference(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("type") String typeStr,
            @RequestParam("enabled") boolean enabled) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        try {
            NotificationType type = NotificationType.valueOf(typeStr);
            notificationService.setPreference(currentUser, type, enabled);
        } catch (IllegalArgumentException e) {
            // ignore invalid type
        }
        return "redirect:/notifications";
    }
}
