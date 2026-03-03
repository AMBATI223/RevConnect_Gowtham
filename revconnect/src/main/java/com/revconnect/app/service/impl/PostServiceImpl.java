package com.revconnect.app.service.impl;

import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.CommentRepository;
import com.revconnect.app.repository.LikeRepository;
import com.revconnect.app.repository.ShareRepository;
import com.revconnect.app.repository.SavedPostRepository;
import com.revconnect.app.repository.PostViewStatsRepository;
import com.revconnect.app.repository.ProductServiceItemRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.ProductServiceItem;
import com.revconnect.app.entity.User;
import com.revconnect.app.service.FileStorageService;
import com.revconnect.app.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ShareRepository shareRepository;
    private final SavedPostRepository savedPostRepository;
    private final PostViewStatsRepository postViewStatsRepository;
    private final ProductServiceItemRepository productRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository,
            FileStorageService fileStorageService,
            CommentRepository commentRepository,
            LikeRepository likeRepository,
            ShareRepository shareRepository,
            SavedPostRepository savedPostRepository,
            PostViewStatsRepository postViewStatsRepository,
            ProductServiceItemRepository productRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.shareRepository = shareRepository;
        this.savedPostRepository = savedPostRepository;
        this.postViewStatsRepository = postViewStatsRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void createPost(String username, String content, String hashtags, MultipartFile imageFile,
            String ctaLabel, String ctaLink, LocalDateTime scheduledFor,
            boolean isPinned, boolean isPromotional, Long taggedProductId) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        String mediaUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            mediaUrl = "/uploads/" + fileName;
        }

        boolean isPublished = scheduledFor == null || !scheduledFor.isAfter(LocalDateTime.now());

        ProductServiceItem taggedProduct = null;
        if (taggedProductId != null) {
            taggedProduct = productRepository.findById(taggedProductId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        }

        Post post = Post.builder()
                .content(content)
                .hashtags(hashtags)
                .author(user)
                .mediaUrl(mediaUrl)
                .ctaLabel(ctaLabel)
                .ctaLink(ctaLink)
                .scheduledFor(scheduledFor)
                .isPinned(isPinned)
                .isPromotional(isPromotional)
                .isPublished(isPublished)
                .taggedProduct(taggedProduct)
                .build();
        postRepository.save(post);
    }

    @Override
    public void updatePost(Long id, String username, String content, String hashtags,
            String ctaLabel, String ctaLink, LocalDateTime scheduledFor,
            boolean isPinned, boolean isPromotional, Long taggedProductId) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to edit this post");
        }

        boolean isPublished = scheduledFor == null || !scheduledFor.isAfter(LocalDateTime.now());

        ProductServiceItem taggedProduct = null;
        if (taggedProductId != null) {
            taggedProduct = productRepository.findById(taggedProductId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        }

        post.setContent(content);
        post.setHashtags(hashtags);
        post.setCtaLabel(ctaLabel);
        post.setCtaLink(ctaLink);
        post.setScheduledFor(scheduledFor);
        post.setPinned(isPinned);
        post.setPromotional(isPromotional);
        post.setPublished(isPublished);
        post.setTaggedProduct(taggedProduct);

        postRepository.save(post);
    }

    @Override
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to delete this post");
        }
        commentRepository.deleteByPost(post);
        likeRepository.deleteByPost(post);
        shareRepository.deleteByPost(post);
        savedPostRepository.deleteByPost(post);
        postViewStatsRepository.deleteByPost(post);
        postRepository.delete(post);
    }

    @Override
    public void togglePin(Long id, String username) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized to pin this post");
        }
        post.setPinned(!post.isPinned());
        postRepository.save(post);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAllByIsPublishedTrueAndAuthorIsPrivateProfileFalseOrderByCreatedAtDesc();
    }

    @Override
    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    @Override
    public List<Post> getPostsByAuthor(User author) {
        List<Post> posts = new java.util.ArrayList<>(
                postRepository.findByAuthorAndIsPublishedTrueOrderByCreatedAtDesc(author));
        posts.sort((p1, p2) -> {
            if (p1.isPinned() == p2.isPinned()) {
                return 0; // maintain relative order since already sorted by date descending from DB
            }
            return p1.isPinned() ? -1 : 1;
        });
        return posts;
    }

    @Override
    public List<Post> getPersonalizedFeed(String username, String postType, com.revconnect.app.entity.Role userRole) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> feed;
        if (userRole != null) {
            feed = postRepository.findPersonalizedFeedWithRole(user, userRole);
        } else {
            feed = postRepository.findPersonalizedFeed(user);
        }

        if (feed == null || feed.isEmpty()) {
            // Fallback for new users: Show public posts from business/creator accounts
            feed = postRepository.findPromotedPublicFeed();
        }

        if (postType != null) {
            if ("media".equals(postType)) {
                feed = feed.stream().filter(p -> p.getMediaUrl() != null && !p.getMediaUrl().isEmpty())
                        .collect(Collectors.toList());
            } else if ("text".equals(postType)) {
                feed = feed.stream().filter(p -> p.getMediaUrl() == null || p.getMediaUrl().isEmpty())
                        .collect(Collectors.toList());
            }
        }
        return feed;
    }

    @Override
    public List<Post> getExploreFeed(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return postRepository.findExploreFeed(user);
    }

    @Override
    public List<String> getTrendingHashtags() {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<String> rawHashtags = postRepository.findRecentHashtags(since);
        return rawHashtags.stream()
                .flatMap(h -> Arrays.stream(h.split("[,\\s]+")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("#") ? s.substring(1) : s)
                .collect(Collectors.groupingBy(s -> s.toLowerCase(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Post> getTrendingPosts() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return postRepository.findTrendingPosts(since).stream().limit(5).collect(Collectors.toList());
    }

    @Override
    public List<Post> searchByHashtag(String hashtag) {
        return postRepository.searchByHashtagPrivacyAware(hashtag);
    }
}
