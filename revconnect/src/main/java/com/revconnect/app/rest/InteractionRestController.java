package com.revconnect.app.rest;

import com.revconnect.app.entity.Comment;
import com.revconnect.app.service.InteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class InteractionRestController {

    private final InteractionService interactionService;

    public InteractionRestController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/post/{id}/like")
    public ResponseEntity<Void> toggleLike(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.toggleLike(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(interactionService.getCommentsForPost(id));
    }

    @PostMapping("/post/{id}/comment")
    public ResponseEntity<Void> addComment(@PathVariable Long id,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.addComment(id, userDetails.getUsername(), content);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.deleteComment(commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
