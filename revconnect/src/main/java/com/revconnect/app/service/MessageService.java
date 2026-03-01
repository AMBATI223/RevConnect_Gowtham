package com.revconnect.app.service;

import com.revconnect.app.entity.Message;
import com.revconnect.app.entity.User;
import java.util.List;

public interface MessageService {
    Message sendMessage(String senderUsername, String receiverUsername, String content);

    List<Message> getConversation(String username1, String username2);

    List<User> getChatPartners(String username);

    void markAsRead(Long messageId, String username);

    int getUnreadMessageCount(String username);
}
