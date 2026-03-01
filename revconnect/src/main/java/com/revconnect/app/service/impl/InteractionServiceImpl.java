package com.revconnect.app.service.impl;

import com.revconnect.app.entity.Comment;
import com.revconnect.app.entity.Like;
import com.revconnect.app.entity.Post;
import com.revconnect.app.entity.Share;
import com.revconnect.app.entity.User;
import com.revconnect.app.repository.CommentRepository;
import com.revconnect.app.repository.LikeRepository;
import com.revconnect.app.repository.PostRepository;
import com.revconnect.app.repository.PostViewStatsRepository;
import com.revconnect.app.repository.ShareRepository;
import com.revconnect.app.repository.UserRepository;
import com.revconnect.app.service.InteractionService;
import com.revconnect.app.service.NotificationService;
import com.revconnect.app.entity.NotificationType;
import com.revconnect.app.entity.PostViewStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InteractionServiceImpl implements InteractionService {
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final ShareRepository shareRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostViewStatsRepository postViewStatsRepository;
    private final NotificationService notificationService;

    public InteractionServiceImpl(LikeRepository likeRepository, CommentRepository commentRepository,
            ShareRepository shareRepository, PostRepository postRepository, UserRepository userRepository,
            PostViewStatsRepository postViewStatsRepository, NotificationService notificationService) {
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.shareRepository = shareRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postViewStatsRepository = postViewStatsRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (likeRepository.existsByPostAndUser(post, user)) {
            likeRepository.deleteByPostAndUser(post, user);
        } else {
            likeRepository.save(Like.builder().post(post).user(user).build());
            if (!post.getAuthor().equals(user)) {
                notificationService.createNotification(post.getAuthor(), NotificationType.LIKE, postId,
                        user.getUsername() + " liked your post");
            }
        }
    }

    @Override
    public void addComment(Long postId, String username, String content) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        commentRepository.save(Comment.builder().post(post).user(user).content(content).build());
        if (!post.getAuthor().equals(user)) {
            notificationService.createNotification(post.getAuthor(), NotificationType.COMMENT, postId,
                    user.getUsername() + " commented on your post: " + content);
        }
    }

    @Override
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        if (comment.getUser().getUsername().equals(username) ||
                comment.getPost().getAuthor().getUsername().equals(username)) {
            commentRepository.delete(comment);
        }
    }

    @Override
    public void sharePost(Long postId, String username) {
        Post originalPost = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        // Don't allow sharing your own post
        if (originalPost.getAuthor().getUsername().equals(username)) {
            return;
        }

        if (!shareRepository.existsByPostAndUser(originalPost, user)) {
            shareRepository.save(Share.builder().post(originalPost).user(user).build());

            // Create a new post as a "repost"
            Post sharedPost = Post.builder()
                    .author(user)
                    .content("") // Reposts can have empty content if it's just a share, or we can add a comment
                                 // field later
                    .parentPost(originalPost)
                    .isPublished(true)
                    .build();

            postRepository.save(sharedPost);

            if (!originalPost.getAuthor().equals(user)) {
                notificationService.createNotification(originalPost.getAuthor(), NotificationType.SHARE,
                        originalPost.getId(),
                        user.getUsername() + " shared your post");
            }
        }
    }

    @Override
    public List<Comment> getCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return commentRepository.findByPostOrderByCreatedAtDesc(post);
    }

    @Override
    public void trackView(Long postId, String username) {
        Post post = postRepository.findById(postId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!postViewStatsRepository.existsByPostAndViewer(post, user)) {
            postViewStatsRepository.save(new PostViewStats(post, user));
        }
    }
}
