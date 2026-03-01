package com.revconnect.app.repository;

import com.revconnect.app.entity.Notification;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
