package com.revconnect.app.rest;

import com.revconnect.app.entity.Post;
import com.revconnect.app.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostRestController {

    private final PostService postService;

    public PostRestController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Post>> getPersonalizedFeed(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(postService.getPersonalizedFeed(userDetails.getUsername(), null, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String content,
            @RequestParam(required = false) String hashtags,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(required = false) String ctaLabel,
            @RequestParam(required = false) String ctaLink,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime scheduledFor,
            @RequestParam(required = false, defaultValue = "false") boolean isPinned,
            @RequestParam(required = false, defaultValue = "false") boolean isPromotional,
            @RequestParam(required = false) Long taggedProductId) {
        postService.createPost(userDetails.getUsername(), content, hashtags, image, ctaLabel, ctaLink, scheduledFor,
                isPinned, isPromotional, taggedProductId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String content, @RequestParam(required = false) String hashtags,
            @RequestParam(required = false) String ctaLabel,
            @RequestParam(required = false) String ctaLink,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime scheduledFor,
            @RequestParam(required = false, defaultValue = "false") boolean isPinned,
            @RequestParam(required = false, defaultValue = "false") boolean isPromotional,
            @RequestParam(required = false) Long taggedProductId) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        postService.updatePost(id, userDetails.getUsername(), content, hashtags, ctaLabel, ctaLink, scheduledFor,
                isPinned, isPromotional, taggedProductId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return ResponseEntity.status(401).build();
        postService.deletePost(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
