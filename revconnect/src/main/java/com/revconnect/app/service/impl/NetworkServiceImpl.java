package com.revconnect.app.service.impl;

import com.revconnect.app.entity.*;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.repository.*;
import com.revconnect.app.service.NetworkService;
import com.revconnect.app.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class NetworkServiceImpl implements NetworkService {
    private static final Logger log = LoggerFactory.getLogger(NetworkServiceImpl.class);
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ConnectionRepository connectionRepository;

    public NetworkServiceImpl(FollowRepository followRepository,
            UserRepository userRepository, NotificationService notificationService,
            ConnectionRepository connectionRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.connectionRepository = connectionRepository;
    }

    @Override
    public List<User> getConnections(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return connectionRepository.findConnectionsByUserAndStatus(user, ConnectionStatus.ACCEPTED)
                .stream()
                .map(c -> c.getSender().equals(user) ? c.getReceiver() : c.getSender())
                .collect(Collectors.toList());
    }

    @Override
    public void sendConnectionRequest(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();

        if (connectionRepository.findBySenderAndReceiver(sender, receiver).isEmpty() && !sender.equals(receiver)) {
            Connection connection = Connection.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .status(ConnectionStatus.PENDING)
                    .build();
            connectionRepository.save(connection);
            notificationService.createNotification(receiver, NotificationType.CONNECTION_REQUEST, connection.getId(),
                    senderUsername + " sent you a connection request");
        }
    }

    @Override
    public void acceptConnection(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();

        connectionRepository.findBySenderAndReceiver(sender, receiver).ifPresent(connection -> {
            connection.setStatus(ConnectionStatus.ACCEPTED);
            connectionRepository.save(connection);

            // Auto-follow each other when connected
            followUser(senderUsername, receiverUsername);
            followUser(receiverUsername, senderUsername);

            notificationService.createNotification(sender, NotificationType.CONNECTION_ACCEPTED, connection.getId(),
                    receiverUsername + " accepted your connection request");
        });
    }

    @Override
    public void rejectConnection(String receiverUsername, String senderUsername) {
        User receiver = userRepository.findByUsername(receiverUsername).orElseThrow();
        User sender = userRepository.findByUsername(senderUsername).orElseThrow();

        connectionRepository.findBySenderAndReceiver(sender, receiver).ifPresent(connection -> {
            connection.setStatus(ConnectionStatus.REJECTED);
            connectionRepository.save(connection);
        });
    }

    @Override
    public List<User> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return connectionRepository.findByReceiverAndStatus(user, ConnectionStatus.PENDING)
                .stream()
                .map(Connection::getSender)
                .collect(Collectors.toList());
    }

    @Override
    public boolean areConnected(String user1, String user2) {
        User u1 = userRepository.findByUsername(user1).orElse(null);
        User u2 = userRepository.findByUsername(user2).orElse(null);
        if (u1 == null || u2 == null)
            return false;

        return connectionRepository.findBySenderAndReceiver(u1, u2)
                .map(c -> c.getStatus() == ConnectionStatus.ACCEPTED)
                .orElse(connectionRepository.findBySenderAndReceiver(u2, u1)
                        .map(c -> c.getStatus() == ConnectionStatus.ACCEPTED)
                        .orElse(false));
    }

    @Override
    public void followUser(String followerUsername, String followingUsername) {
        log.info("FOLLOW: {} -> {}", followerUsername, followingUsername);
        User follower = userRepository.findByUsername(followerUsername).orElseThrow();
        User following = userRepository.findByUsername(followingUsername).orElseThrow();

        // Removed Business/Creator role restriction for basic CRUD reversion
        if (followRepository.findByFollowerAndFollowing(follower, following).isEmpty()
                && !follower.equals(following)) {
            Follow follow = Follow.builder().follower(follower).following(following).build();
            followRepository.save(follow);
            log.info("FOLLOW SAVED: {}", follow.getId());
            notificationService.createNotification(following, NotificationType.NEW_FOLLOWER, follow.getId(),
                    followerUsername + " started following you");
        } else {
            log.info("FOLLOW SKIPPED: Already following or self-follow");
        }
    }

    @Override
    public void unfollowUser(String followerUsername, String followingUsername) {
        log.info("UNFOLLOW: {} -> {}", followerUsername, followingUsername);
        User follower = userRepository.findByUsername(followerUsername).orElseThrow();
        User following = userRepository.findByUsername(followingUsername).orElseThrow();
        followRepository.findByFollowerAndFollowing(follower, following).ifPresent(f -> {
            followRepository.delete(f);
            log.info("UNFOLLOW DELETED: {}", f.getId());
        });
    }

    @Override
    public List<User> getFollowers(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<User> result = followRepository.findByFollowing(user).stream().map(Follow::getFollower)
                .collect(Collectors.toList());
        log.info("GET FOLLOWERS for {}: {}", username, result.size());
        return result;
    }

    @Override
    public List<User> getFollowing(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<User> result = followRepository.findByFollower(user).stream().map(Follow::getFollowing)
                .collect(Collectors.toList());
        log.info("GET FOLLOWING for {}: {}", username, result.size());
        return result;
    }

    @Override
    public boolean isPending(String sender, String receiver) {
        User u1 = userRepository.findByUsername(sender).orElse(null);
        User u2 = userRepository.findByUsername(receiver).orElse(null);
        if (u1 == null || u2 == null)
            return false;

        return connectionRepository.findBySenderAndReceiver(u1, u2)
                .map(c -> c.getStatus() == ConnectionStatus.PENDING)
                .orElse(false);
    }

    @Override
    public List<User> getSuggestedProfiles(String username) {
        return userRepository.findSuggestedProfiles(username);
    }
}
