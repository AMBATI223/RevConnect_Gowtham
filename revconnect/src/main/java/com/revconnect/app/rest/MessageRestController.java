package com.revconnect.app.rest;

import com.revconnect.app.entity.Message;
import com.revconnect.app.entity.User;
import com.revconnect.app.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    private final MessageService messageService;

    public MessageRestController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send/{username}")
    public ResponseEntity<Message> sendMessage(@PathVariable String username,
            @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        Message message = messageService.sendMessage(userDetails.getUsername(), username, content);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/chat/{username}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getConversation(userDetails.getUsername(), username));
    }

    @GetMapping("/partners")
    public ResponseEntity<List<User>> getChatPartners(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getChatPartners(userDetails.getUsername()));
    }

    @PostMapping("/read/{id}")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.getUnreadMessageCount(userDetails.getUsername()));
    }
}
