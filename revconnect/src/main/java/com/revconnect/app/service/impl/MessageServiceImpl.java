package com.revconnect.app.service.impl;

import com.revconnect.app.entity.Message;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.MessageRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.MessageService;
import com.revconnect.app.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository,
            NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Message sendMessage(String senderUsername, String receiverUsername, String content) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("Cannot send message to yourself");
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        // Notifications for messages
        notificationService.createNotification(receiver, com.revconnect.app.entity.NotificationType.MESSAGE,
                message.getId(), "New message from " + sender.getUsername());

        return message;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getConversation(String username1, String username2) {
        User user1 = userRepository.findByUsername(username1)
                .orElseThrow(() -> new RuntimeException("User 1 not found"));
        User user2 = userRepository.findByUsername(username2)
                .orElseThrow(() -> new RuntimeException("User 2 not found"));
        return messageRepository.findConversation(user1, user2);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getChatPartners(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        List<User> senders = messageRepository.findDistinctSendersByReceiver(user);
        List<User> receivers = messageRepository.findDistinctReceiversBySender(user);
        java.util.Set<User> combined = new java.util.LinkedHashSet<>();
        if (senders != null)
            combined.addAll(senders);
        if (receivers != null)
            combined.addAll(receivers);
        return new java.util.ArrayList<>(combined);
    }

    @Override
    public void markAsRead(Long messageId, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (message.getReceiver().getUsername().equals(username)) {
            message.setRead(true);
            messageRepository.save(message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadMessageCount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }
}
