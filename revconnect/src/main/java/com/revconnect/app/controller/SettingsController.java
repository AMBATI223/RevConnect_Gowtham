package com.revconnect.app.controller;

import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SettingsController(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public String viewSettings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("preferences", notificationService.getPreferences(user));
        return "settings";
    }

    @PostMapping("/notifications")
    public String updateNotificationPreferences(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Map<String, String> allParams) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (NotificationType type : NotificationType.values()) {
            boolean enabled = allParams.containsKey(type.name()) && "on".equals(allParams.get(type.name()));
            notificationService.setPreference(user, type, enabled);
        }

        return "redirect:/settings?success=notifications";
    }

    @PostMapping("/privacy")
    public String updatePrivacySettings(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "false") boolean isPrivateProfile) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPrivateProfile(isPrivateProfile);
        userRepository.save(user);

        return "redirect:/settings?success=privacy";
    }
}
