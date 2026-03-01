package com.revconnect.app.controller;

import com.revconnect.app.entity.Role;
import com.revconnect.app.service.PostService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    private final PostService postService;
    private final com.revconnect.app.repository.UserRepository userRepository;
    private final com.revconnect.app.service.NetworkService networkService;

    public IndexController(PostService postService,
            com.revconnect.app.repository.UserRepository userRepository,
            com.revconnect.app.service.NetworkService networkService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.networkService = networkService;
    }

    @GetMapping("/")
    public String index(Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) Role userType) {

        if (userDetails != null) {
            com.revconnect.app.entity.User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                model.addAttribute("currentUser", user);
                model.addAttribute("followersCount", networkService.getFollowers(user.getUsername()).size());
                model.addAttribute("followingCount", networkService.getFollowing(user.getUsername()).size());
                model.addAttribute("pendingCount", networkService.getPendingRequests(user.getUsername()).size());
            }
            model.addAttribute("posts", postService.getPersonalizedFeed(userDetails.getUsername(), postType, userType));
        } else {
            model.addAttribute("posts", postService.getAllPosts());
        }

        model.addAttribute("trendingHashtags", postService.getTrendingHashtags());
        model.addAttribute("trendingPosts", postService.getTrendingPosts());
        return "index";
    }
}
