package com.revconnect.app.controller;

import com.revconnect.app.service.InteractionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/interactions")
public class InteractionController {
    private final InteractionService interactionService;
    private final com.revconnect.app.repository.PostRepository postRepository;

    public InteractionController(final InteractionService interactionService,
            com.revconnect.app.repository.PostRepository postRepository) {
        this.interactionService = interactionService;
        this.postRepository = postRepository;
    }

    @PostMapping("/post/{id}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLike(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpServletRequest request) {
        if (userDetails == null) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please log in"));
            }
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/login")).build();
        }

        long count = interactionService.toggleLike(id, userDetails.getUsername());

        if ("XMLHttpRequest".equals(requestedWith)) {
            Map<String, Object> response = new HashMap<>();
            response.put("likesCount", count);
            return ResponseEntity.ok(response);
        }

        String referer = request.getHeader("Referer");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(referer != null ? referer : "/")).build();
    }

    @PostMapping("/post/{id}/comment")
    @ResponseBody
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpServletRequest request) {
        if (userDetails == null) {
            if ("XMLHttpRequest".equals(requestedWith)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Please log in"));
            }
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/login")).build();
        }

        com.revconnect.app.entity.Comment comment = interactionService.addComment(id, userDetails.getUsername(),
                content);
        long count = interactionService.countCommentsByPost(id);

        if ("XMLHttpRequest".equals(requestedWith)) {
            Map<String, Object> response = new HashMap<>();
            response.put("commentsCount", count);
            response.put("author", comment.getUser().getUsername());
            response.put("content", comment.getContent());
            return ResponseEntity.ok(response);
        }

        String referer = request.getHeader("Referer");
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(referer != null ? referer : "/posts/" + id)).build();
    }

    @PostMapping("/comment/{id}/delete")
    public String deleteComment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long postId) {
        interactionService.deleteComment(id, userDetails.getUsername());
        return postId != null ? "redirect:/posts/" + postId : "redirect:/";
    }

    @PostMapping("/post/{id}/share")
    public String sharePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.sharePost(id, userDetails.getUsername());
        return "redirect:/";
    }

    @PostMapping("/post/{id}/view")
    @ResponseBody
    public ResponseEntity<Void> trackView(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            interactionService.trackView(id, userDetails.getUsername());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{id}/comments")
    @ResponseBody
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        List<com.revconnect.app.entity.Comment> comments = interactionService.getCommentsForPost(id);
        List<Map<String, String>> dtos = comments.stream().map(c -> {
            Map<String, String> map = new HashMap<>();
            map.put("author", c.getUser().getUsername());
            map.put("authorImage", c.getUser().getProfilePictureUrl() != null ? c.getUser().getProfilePictureUrl()
                    : "https://via.placeholder.com/32");
            map.put("content", c.getContent());
            if (c.getCreatedAt() != null) {
                map.put("createdAt", c.getCreatedAt().toString());
            } else {
                map.put("createdAt", "");
            }
            return map;
        }).toList();
        return ResponseEntity.ok(dtos);
    }
}
