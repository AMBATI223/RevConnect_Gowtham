package com.revconnect.app.repository;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.PostViewStats;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewStatsRepository extends JpaRepository<PostViewStats, Long> {
    boolean existsByPostAndViewer(Post post, User viewer);

    long countByPost(Post post);

    java.util.List<PostViewStats> findByPost(Post post);

    void deleteByPost(Post post);
}
