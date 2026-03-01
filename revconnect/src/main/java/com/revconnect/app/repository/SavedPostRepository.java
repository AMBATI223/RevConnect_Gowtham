package com.revconnect.app.repository;

import com.revconnect.app.entity.SavedPost;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    List<SavedPost> findByUserOrderBySavedAtDesc(User user);

    Optional<SavedPost> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    java.util.List<SavedPost> findByPost(Post post);

    void deleteByPost(Post post);
}
