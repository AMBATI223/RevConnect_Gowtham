package com.revconnect.app.repository;

import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT u FROM User u WHERE u.username != :username AND u.isPrivateProfile = false " +
            "AND u NOT IN (SELECT f.following FROM Follow f WHERE f.follower.username = :username) " +
            "AND (u.role = com.revconnect.app.entity.Role.BUSINESS OR u.role = com.revconnect.app.entity.Role.CREATOR) "
            +
            "ORDER BY u.id DESC")
    List<User> findSuggestedProfiles(@Param("username") String username);
}
