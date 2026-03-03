package com.revconnect.app;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Profile("!test")
public class TestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TestRunner.class);

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public TestRunner(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("============== DEBUG POSTS FOR BABU ==============");
        Optional<User> babuOpt = userRepository.findByUsername("babu");
        if (babuOpt.isPresent()) {
            User babu = babuOpt.get();
            List<Post> posts = postRepository.findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(babu);
            log.info("Total published posts for babu: {}", posts.size());
            for (Post p : posts) {
                log.info("ID: {}, Content: '{}', isPromo: {}, isPub: {}",
                        p.getId(), p.getContent(), p.isPromotional(), p.isPublished());
            }
        } else {
            log.info("User babu not found.");
        }
        log.info("==================================================");
    }
}
