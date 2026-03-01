package com.revconnect.app.rest;

import com.revconnect.app.entity.SavedPost;
import com.revconnect.app.service.SavedPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved")
public class SavedPostRestController {

    private final SavedPostService savedPostService;

    public SavedPostRestController(SavedPostService savedPostService) {
        this.savedPostService = savedPostService;
    }

    @GetMapping
    public ResponseEntity<List<SavedPost>> getSavedPosts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(savedPostService.getSavedPosts(userDetails.getUsername()));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> toggleSavePost(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        savedPostService.toggleSavePost(userDetails.getUsername(), id);
        return ResponseEntity.ok().build();
    }
}
