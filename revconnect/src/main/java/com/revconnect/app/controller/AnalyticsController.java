package com.revconnect.app.controller;

import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import com.revconnect.app.service.NetworkService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnalyticsController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NetworkService networkService;

    public AnalyticsController(UserRepository userRepository, PostRepository postRepository,
            NetworkService networkService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.networkService = networkService;
    }

    @GetMapping("/analytics")
    public String getAnalytics(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == null || (user.getRole() != com.revconnect.app.entity.Role.BUSINESS
                && user.getRole() != com.revconnect.app.entity.Role.CREATOR)) {
            return "redirect:/"; // Only Creator and Business accounts have access to Analytics
        }

        List<Post> userPosts = postRepository.findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(user);

        long totalViews = 0;
        long totalLikes = 0;
        long totalComments = 0;
        long totalShares = 0;
        Map<Long, Long> postViews = new HashMap<>();

        for (Post post : userPosts) {
            postViews.put(post.getId(), (long) post.getReachCount());
            totalViews += post.getReachCount();
            totalLikes += post.getLikesCount();
            totalComments += post.getCommentsCount();
            totalShares += post.getSharesCount();
        }

        List<User> followers = networkService.getFollowers(user.getUsername());
        long totalFollowers = followers.size();

        Map<String, Long> locationDemographics = new HashMap<>();
        Map<String, Long> industryDemographics = new HashMap<>();

        for (User follower : followers) {
            // Location demographics
            String location = follower.getLocation();
            if (location == null || location.trim().isEmpty()) {
                location = "Unknown";
            }
            locationDemographics.put(location, locationDemographics.getOrDefault(location, 0L) + 1);

            // Industry demographics (if follower has business profile)
            if (follower.getBusinessProfile() != null) {
                String industry = follower.getBusinessProfile().getCategory();
                if (industry != null && !industry.trim().isEmpty()) {
                    industryDemographics.put(industry, industryDemographics.getOrDefault(industry, 0L) + 1);
                }
            }
        }

        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalLikes", totalLikes);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("totalShares", totalShares);
        model.addAttribute("totalEngagement", totalLikes + totalComments + totalShares);
        model.addAttribute("totalFollowers", totalFollowers);
        model.addAttribute("posts", userPosts);
        model.addAttribute("postViews", postViews);
        model.addAttribute("locationDemographics", locationDemographics);
        model.addAttribute("industryDemographics", industryDemographics);

        return "analytics";
    }
}
