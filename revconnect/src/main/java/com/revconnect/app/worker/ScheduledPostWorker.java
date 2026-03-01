package com.revconnect.app.worker;

import com.revconnect.app.entity.Post;
import com.revconnect.app.repository.PostRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScheduledPostWorker {

    private final PostRepository postRepository;

    public ScheduledPostWorker(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void publishScheduledPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> postsToPublish = postRepository.findByIsPublishedFalseAndScheduledForLessThanEqual(now);

        for (Post post : postsToPublish) {
            post.setPublished(true);
            postRepository.save(post);
            System.out.println("Published scheduled post ID: " + post.getId());
        }
    }
}
