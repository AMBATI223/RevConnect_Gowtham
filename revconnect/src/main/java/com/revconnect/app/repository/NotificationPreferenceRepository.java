package com.revconnect.app.repository;

import com.revconnect.app.entity.NotificationPreference;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserAndType(User user, NotificationType type);
}
