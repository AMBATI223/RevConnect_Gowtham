package com.revconnect.app.repository;

import com.revconnect.app.entity.Like;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);

    long countByPost(Post post);

    java.util.List<Like> findByPost(Post post);

    void deleteByPost(Post post);
}
