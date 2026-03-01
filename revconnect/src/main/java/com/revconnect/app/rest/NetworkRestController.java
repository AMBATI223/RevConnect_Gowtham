package com.revconnect.app.rest;

import com.revconnect.app.entity.User;
import com.revconnect.app.service.NetworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/network")
public class NetworkRestController {

    private final NetworkService networkService;

    public NetworkRestController(NetworkService networkService) {
        this.networkService = networkService;
    }

    @PostMapping("/follow/{username}")
    public ResponseEntity<Void> followUser(@PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        networkService.followUser(userDetails.getUsername(), username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unfollow/{username}")
    public ResponseEntity<Void> unfollowUser(@PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        networkService.unfollowUser(userDetails.getUsername(), username);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/followers")
    public ResponseEntity<List<User>> getFollowers(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(networkService.getFollowers(userDetails.getUsername()));
    }

    @GetMapping("/following")
    public ResponseEntity<List<User>> getFollowing(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(networkService.getFollowing(userDetails.getUsername()));
    }
}
