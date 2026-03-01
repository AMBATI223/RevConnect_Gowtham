package com.revconnect.app.repository;

import com.revconnect.app.entity.Follow;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    List<Follow> findByFollowing(User following);

    List<Follow> findByFollower(User follower);

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
}
