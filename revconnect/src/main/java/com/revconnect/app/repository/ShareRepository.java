package com.revconnect.app.repository;

import com.revconnect.app.entity.Share;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
    boolean existsByPostAndUser(Post post, User user);

    long countByPost(Post post);

    java.util.List<Share> findByPost(Post post);

    void deleteByPost(Post post);
}
