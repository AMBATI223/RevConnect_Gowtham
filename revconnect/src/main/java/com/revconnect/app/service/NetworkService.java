package com.revconnect.app.service;

import com.revconnect.app.entity.User;
import java.util.List;

public interface NetworkService {
    List<User> getConnections(String username);

    void followUser(String followerUsername, String followingUsername);

    void unfollowUser(String followerUsername, String followingUsername);

    List<User> getFollowers(String username);

    List<User> getFollowing(String username);

    void sendConnectionRequest(String senderUsername, String receiverUsername);

    void acceptConnection(String receiverUsername, String senderUsername);

    void rejectConnection(String receiverUsername, String senderUsername);

    List<User> getPendingRequests(String username);

    boolean areConnected(String user1, String user2);

    boolean isPending(String sender, String receiver);
}
