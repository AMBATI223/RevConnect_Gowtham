package com.revconnect.app;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

@SpringBootTest
public class PostCountTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void testBabuPosts() {
        System.out.println("====== DB CHECK FOR BABU'S POSTS ======");
        Optional<User> babuOpt = userRepository.findByUsername("babu");
        if (babuOpt.isPresent()) {
            User babu = babuOpt.get();
            List<Post> allPosts = postRepository.findAll();
            int count = 0;
            for (Post p : allPosts) {
                if (p.getAuthor() != null && p.getAuthor().getId().equals(babu.getId())) {
                    System.out.println("ID: " + p.getId() +
                            " | Content: '" + p.getContent() + "'" +
                            " | Published: " + p.isPublished() +
                            " | Scheduled: " + p.getScheduledFor() +
                            " | Parent: " + (p.getParentPost() != null ? p.getParentPost().getId() : "none"));
                    count++;
                }
            }
            System.out.println("Total posts found in DB for babu (regardless of publish state): " + count);

            System.out.println("--- Now testing PostService Query ---");
            List<Post> publishedPosts = postRepository.findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(babu);
            System.out.println("Total published posts found via repository query: " + publishedPosts.size());
        } else {
            System.out.println("User 'babu' not found in database.");
        }
        System.out.println("=======================================");
    }
}
