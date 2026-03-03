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
    private final com.revconnect.app.repository.ProductServiceItemRepository productRepository;
    private final com.revconnect.app.service.InteractionService interactionService;

    public IndexController(PostService postService,
            com.revconnect.app.repository.UserRepository userRepository,
            com.revconnect.app.service.NetworkService networkService,
            com.revconnect.app.repository.ProductServiceItemRepository productRepository,
            com.revconnect.app.service.InteractionService interactionService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.networkService = networkService;
        this.productRepository = productRepository;
        this.interactionService = interactionService;
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

                if (user.getRole() == com.revconnect.app.entity.Role.BUSINESS
                        || user.getRole() == com.revconnect.app.entity.Role.CREATOR) {
                    if (user.getBusinessProfile() != null) {
                        model.addAttribute("products",
                                productRepository.findByBusinessProfile(user.getBusinessProfile()));
                    }
                }
            }
            java.util.List<com.revconnect.app.entity.Post> feedPosts = postService
                    .getPersonalizedFeed(userDetails.getUsername(), postType, userType);
            for (com.revconnect.app.entity.Post p : feedPosts) {
                interactionService.trackView(p.getId(), userDetails.getUsername());
            }
            model.addAttribute("posts", feedPosts);
            model.addAttribute("suggestedProfiles", networkService.getSuggestedProfiles(userDetails.getUsername()));
        } else {
            return "home";
        }

        model.addAttribute("trendingHashtags", postService.getTrendingHashtags());
        model.addAttribute("trendingPosts", postService.getTrendingPosts());
        return "index";
    }
}
