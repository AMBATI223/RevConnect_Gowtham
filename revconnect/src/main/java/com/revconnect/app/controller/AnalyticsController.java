package com.revconnect.app.controller;

import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.PostViewStatsRepository;
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
    private final PostViewStatsRepository postViewStatsRepository;
    private final NetworkService networkService;

    public AnalyticsController(UserRepository userRepository, PostRepository postRepository,
            PostViewStatsRepository postViewStatsRepository, NetworkService networkService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postViewStatsRepository = postViewStatsRepository;
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
        Map<Long, Long> postViews = new HashMap<>();

        for (Post post : userPosts) {
            long views = postViewStatsRepository.countByPost(post);
            postViews.put(post.getId(), views);

            totalViews += views;
            totalLikes += post.getLikesCount();
            totalComments += post.getCommentsCount();
        }

        long totalFollowers = networkService.getFollowers(user.getUsername()).size();

        model.addAttribute("totalViews", totalViews);
        model.addAttribute("totalLikes", totalLikes);
        model.addAttribute("totalComments", totalComments);
        model.addAttribute("totalEngagement", totalLikes + totalComments);
        model.addAttribute("totalFollowers", totalFollowers);
        model.addAttribute("posts", userPosts);
        model.addAttribute("postViews", postViews);

        return "analytics";
    }
}
