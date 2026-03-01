package com.revconnect.app.service;

import com.revconnect.app.entity.SavedPost;
import java.util.List;

public interface SavedPostService {
    void toggleSavePost(String username, Long postId);

    List<SavedPost> getSavedPosts(String username);
}
