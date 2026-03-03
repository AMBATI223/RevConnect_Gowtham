package com.revconnect.app.repository;

import com.revconnect.app.entity.Comment;
import com.revconnect.app.entity.Like;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.Role;
import com.revconnect.app.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("author");
        testUser.setEmail("author@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void countTotalLikesByAuthor_ReturnsCorrectSum() {
        Post post1 = new Post();
        post1.setAuthor(testUser);
        post1.setContent("Post 1");
        post1.setPublished(true);
        entityManager.persist(post1);

        Post post2 = new Post();
        post2.setAuthor(testUser);
        post2.setContent("Post 2");
        post2.setPublished(true);
        entityManager.persist(post2);

        // Add 10 likes to post1
        for (int i = 0; i < 10; i++) {
            User liker = new User();
            liker.setUsername("liker_p1_" + i);
            liker.setEmail("liker_p1_" + i + "@example.com");
            liker.setPassword("password");
            liker.setRole(Role.USER);
            entityManager.persist(liker);

            Like like = new Like();
            like.setPost(post1);
            like.setUser(liker);
            entityManager.persist(like);
        }

        // Add 5 likes to post2
        for (int i = 0; i < 5; i++) {
            User liker = new User();
            liker.setUsername("liker_p2_" + i);
            liker.setEmail("liker_p2_" + i + "@example.com");
            liker.setPassword("password");
            liker.setRole(Role.USER);
            entityManager.persist(liker);

            Like like = new Like();
            like.setPost(post2);
            like.setUser(liker);
            entityManager.persist(like);
        }

        entityManager.flush();

        long totalLikes = postRepository.countTotalLikesByAuthor(testUser);
        assertEquals(15, totalLikes);
    }

    @Test
    void countTotalCommentsByAuthor_ReturnsCorrectSum() {
        Post post1 = new Post();
        post1.setAuthor(testUser);
        post1.setContent("Post 1");
        post1.setPublished(true);
        entityManager.persist(post1);

        Post post2 = new Post();
        post2.setAuthor(testUser);
        post2.setContent("Post 2");
        post2.setPublished(true);
        entityManager.persist(post2);

        // Add 3 comments to post1
        for (int i = 0; i < 3; i++) {
            User commenter = new User();
            commenter.setUsername("commenter_p1_" + i);
            commenter.setEmail("commenter_p1_" + i + "@example.com");
            commenter.setPassword("password");
            commenter.setRole(Role.USER);
            entityManager.persist(commenter);

            Comment comment = new Comment();
            comment.setPost(post1);
            comment.setUser(commenter);
            comment.setContent("Comment " + i);
            entityManager.persist(comment);
        }

        // Add 7 comments to post2
        for (int i = 0; i < 7; i++) {
            User commenter = new User();
            commenter.setUsername("commenter_p2_" + i);
            commenter.setEmail("commenter_p2_" + i + "@example.com");
            commenter.setPassword("password");
            commenter.setRole(Role.USER);
            entityManager.persist(commenter);

            Comment comment = new Comment();
            comment.setPost(post2);
            comment.setUser(commenter);
            comment.setContent("Comment " + i);
            entityManager.persist(comment);
        }

        entityManager.flush();

        long totalComments = postRepository.countTotalCommentsByAuthor(testUser);
        assertEquals(10, totalComments);
    }

    @Test
    void findPersonalizedFeed_ReturnsCorrectPosts() {
        // Create another user to follow
        User friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPassword("password");
        friend.setRole(Role.USER);
        entityManager.persist(friend);

        Post friendPost = new Post();
        friendPost.setAuthor(friend);
        friendPost.setContent("Friend's post");
        friendPost.setPublished(true);
        friendPost.setCreatedAt(LocalDateTime.now());
        entityManager.persist(friendPost);

        entityManager.flush();

        // Feed for testUser should include friend's post if public or followed
        // Here friend is public by default in this setup (isPrivateProfile=false)
        List<Post> feed = postRepository.findPersonalizedFeed(testUser);
        assertTrue(feed.stream().anyMatch(p -> p.getAuthor().getUsername().equals("friend")));
    }
}
