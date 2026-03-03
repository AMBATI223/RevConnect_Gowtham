package com.revconnect.app.repository;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
        List<Post> findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(User author);

        List<Post> findAllByIsPublishedTrueOrderByCreatedAtDesc();

        List<Post> findAllByIsPublishedTrueAndAuthorIsPrivateProfileFalseOrderByCreatedAtDesc();

        List<Post> findByIsPublishedFalseAndScheduledForLessThanEqual(LocalDateTime now);

        @Query("SELECT p FROM Post p " +
                        "LEFT JOIN Follow f ON (f.following = p.author AND f.follower = :user) " +
                        "WHERE p.isPublished = true " +
                        "AND (p.author = :user OR p.author.isPrivateProfile = false OR f.id IS NOT NULL) " +
                        "ORDER BY p.createdAt DESC")
        List<Post> findPersonalizedFeed(@Param("user") User user);

        @Query("SELECT p FROM Post p " +
                        "LEFT JOIN Follow f ON (f.following = p.author AND f.follower = :user) " +
                        "WHERE p.isPublished = true " +
                        "AND (p.author = :user OR p.author.isPrivateProfile = false OR f.id IS NOT NULL) " +
                        "AND p.author.role = :role " +
                        "ORDER BY p.createdAt DESC")
        List<Post> findPersonalizedFeedWithRole(@Param("user") User user,
                        @Param("role") com.revconnect.app.entity.Role role);

        @Query("SELECT p FROM Post p " +
                        "LEFT JOIN Follow f ON (f.following = p.author AND f.follower = :user) " +
                        "WHERE p.isPublished = true AND p.author != :user " +
                        "AND p.author.isPrivateProfile = false " +
                        "AND f.id IS NULL " +
                        "ORDER BY p.createdAt DESC")
        List<Post> findExploreFeed(@Param("user") User user);

        @Query("SELECT p.hashtags FROM Post p WHERE p.hashtags IS NOT NULL AND p.hashtags != '' AND p.createdAt >= :since")
        List<String> findRecentHashtags(@Param("since") LocalDateTime since);

        @Query("SELECT p FROM Post p WHERE LOWER(p.hashtags) LIKE LOWER(CONCAT('%', :hashtag, '%')) AND p.isPublished = true AND p.author.isPrivateProfile = false ORDER BY p.createdAt DESC")
        List<Post> searchByHashtagPrivacyAware(@Param("hashtag") String hashtag);

        @Query("SELECT p FROM Post p WHERE p.isPublished = true AND p.author.isPrivateProfile = false AND p.createdAt >= :since ORDER BY (p.likesCount + p.commentsCount * 2) DESC")
        List<Post> findTrendingPosts(@Param("since") LocalDateTime since);

        @Query("SELECT p FROM Post p WHERE p.isPublished = true AND p.author.isPrivateProfile = false AND (p.author.role = com.revconnect.app.entity.Role.BUSINESS OR p.author.role = com.revconnect.app.entity.Role.CREATOR) ORDER BY p.createdAt DESC")
        List<Post> findPromotedPublicFeed();

        @Query("SELECT COALESCE(SUM(p.likesCount), 0) FROM Post p WHERE p.author = :author")
        long countTotalLikesByAuthor(@Param("author") User author);

        @Query("SELECT COALESCE(SUM(p.commentsCount), 0) FROM Post p WHERE p.author = :author")
        long countTotalCommentsByAuthor(@Param("author") User author);
}
