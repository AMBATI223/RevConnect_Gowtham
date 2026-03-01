package com.revconnect.app.service.impl;

import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.SavedPostRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.SavedPost;
import com.revconnect.app.entity.User;
import com.revconnect.app.service.SavedPostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SavedPostServiceImpl implements SavedPostService {
    private final SavedPostRepository savedPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public SavedPostServiceImpl(SavedPostRepository savedPostRepository, UserRepository userRepository,
            PostRepository postRepository) {
        this.savedPostRepository = savedPostRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public void toggleSavePost(String username, Long postId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Optional<SavedPost> existingSave = savedPostRepository.findByUserAndPost(user, post);
        if (existingSave.isPresent()) {
            savedPostRepository.delete(existingSave.get());
        } else {
            SavedPost savedPost = SavedPost.builder().user(user).post(post).build();
            savedPostRepository.save(savedPost);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedPost> getSavedPosts(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return savedPostRepository.findByUserOrderBySavedAtDesc(user);
    }
}
