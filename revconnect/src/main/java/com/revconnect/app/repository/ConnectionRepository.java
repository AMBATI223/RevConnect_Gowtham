package com.revconnect.app.repository;

import com.revconnect.app.entity.Connection;
import com.revconnect.app.entity.ConnectionStatus;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    List<Connection> findByReceiverAndStatus(User receiver, ConnectionStatus status);

    @Query("SELECT c FROM Connection c WHERE (c.sender = :user OR c.receiver = :user) AND c.status = :status")
    List<Connection> findConnectionsByUserAndStatus(@Param("user") User user, @Param("status") ConnectionStatus status);

    Optional<Connection> findBySenderAndReceiver(User sender, User receiver);

    List<Connection> findBySenderAndStatus(User sender, ConnectionStatus status);
}
