package com.revconnect.app.service;

import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.User;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PostService {
        void createPost(String username, String content, String hashtags, MultipartFile imageFile,
                        String ctaLabel, String ctaLink, java.time.LocalDateTime scheduledFor,
                        boolean isPinned, boolean isPromotional, Long taggedProductId);

        void updatePost(Long id, String username, String content, String hashtags,
                        String ctaLabel, String ctaLink, java.time.LocalDateTime scheduledFor,
                        boolean isPinned, boolean isPromotional, Long taggedProductId);

        void deletePost(Long id, String username);

        void togglePin(Long id, String username);

        List<Post> getAllPosts();

        Post getPostById(Long id);

        List<Post> getPostsByAuthor(User author);

        List<Post> getPersonalizedFeed(String username, String postType, com.revconnect.app.entity.Role userRole);

        List<Post> getExploreFeed(String username);

        List<String> getTrendingHashtags();

        List<Post> getTrendingPosts();

        List<Post> searchByHashtag(String hashtag);
}
