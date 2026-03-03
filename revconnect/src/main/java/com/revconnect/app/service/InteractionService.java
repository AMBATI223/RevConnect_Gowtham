package com.revconnect.app.service;

import com.revconnect.app.entity.Comment;
import java.util.List;

public interface InteractionService {
    long toggleLike(Long postId, String username);

    Comment addComment(Long postId, String username, String content);

    void deleteComment(Long commentId, String username);

    void sharePost(Long postId, String username);

    List<Comment> getCommentsForPost(Long postId);

    void trackView(Long postId, String username);

    long countCommentsByPost(Long postId);
}
