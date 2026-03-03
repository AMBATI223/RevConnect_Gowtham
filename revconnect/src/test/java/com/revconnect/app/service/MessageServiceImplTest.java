package com.revconnect.app.service;

import com.revconnect.app.entity.Message;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.MessageRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageServiceImpl(messageRepository, userRepository, notificationService);
    }

    private User createUser(String username, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(Role.USER);
        return user;
    }

    @Test
    void sendMessage_ValidInput_ShouldSaveMessage() {
        User sender = createUser("don", 1L);
        User receiver = createUser("receiver", 2L);

        when(userRepository.findByUsername("don")).thenReturn(Optional.of(sender));
        when(userRepository.findByUsername("receiver")).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(10L);
            return m;
        });

        Message sent = messageService.sendMessage("don", "receiver", "Hello!");

        assertNotNull(sent);
        assertEquals("Hello!", sent.getContent());
        verify(messageRepository).save(any(Message.class));
        verify(notificationService).createNotification(eq(receiver), eq(NotificationType.MESSAGE), eq(10L),
                anyString());
    }

    @Test
    void getConversation_ValidUsers_ShouldReturnMessages() {
        User u1 = createUser("user1", 1L);
        User u2 = createUser("user2", 2L);

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(u1));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(u2));
        when(messageRepository.findConversation(u1, u2)).thenReturn(List.of(new Message()));

        List<Message> conversation = messageService.getConversation("user1", "user2");

        assertFalse(conversation.isEmpty());
        verify(messageRepository).findConversation(u1, u2);
    }

    @Test
    void markAsRead_ValidUser_ShouldUpdateMessage() {
        User receiver = createUser("user1", 1L);
        Message message = new Message();
        message.setReceiver(receiver);
        message.setRead(false);

        when(messageRepository.findById(10L)).thenReturn(Optional.of(message));

        messageService.markAsRead(10L, "user1");

        assertTrue(message.isRead());
        verify(messageRepository).save(message);
    }
}
