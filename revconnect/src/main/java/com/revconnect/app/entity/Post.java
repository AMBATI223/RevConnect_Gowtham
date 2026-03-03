package com.revconnect.app.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "posts")
public class Post {
    @Id
    @SequenceGenerator(name = "post_seq", sequenceName = "POST_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    @Column(name = "POST_ID")
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    private String hashtags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String mediaUrl;

    @org.hibernate.annotations.Formula("(SELECT COUNT(1) FROM post_likes l WHERE l.post_id = post_id)")
    private int likesCount;

    @org.hibernate.annotations.Formula("(SELECT COUNT(1) FROM comments c WHERE c.post_id = post_id)")
    private int commentsCount;

    private String ctaLabel;
    private String ctaLink;
    private LocalDateTime scheduledFor;
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;
    @Column(name = "is_promotional", nullable = false)
    private boolean isPromotional = false;
    @Column(name = "is_published", nullable = false)
    private boolean isPublished = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private Post parentPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductServiceItem taggedProduct;

    private long reachCount;
    private long sharesCount;

    public Post() {
    }

    public Post(Long id, String content, String hashtags, User author, LocalDateTime createdAt, String mediaUrl,
            int likesCount, int commentsCount, String ctaLabel, String ctaLink, LocalDateTime scheduledFor,
            boolean isPinned, boolean isPromotional, boolean isPublished, Post parentPost,
            ProductServiceItem taggedProduct, long reachCount, long sharesCount) {
        this.id = id;
        this.content = content;
        this.hashtags = hashtags;
        this.author = author;
        this.createdAt = createdAt;
        this.mediaUrl = mediaUrl;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.ctaLabel = ctaLabel;
        this.ctaLink = ctaLink;
        this.scheduledFor = scheduledFor;
        this.isPinned = isPinned;
        this.isPromotional = isPromotional;
        this.isPublished = isPublished;
        this.parentPost = parentPost;
        this.taggedProduct = taggedProduct;
        this.reachCount = reachCount;
        this.sharesCount = sharesCount;
    }

    public static PostBuilder builder() {
        return new PostBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHashtags() {
        return hashtags;
    }

    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getCtaLabel() {
        return ctaLabel;
    }

    public void setCtaLabel(String ctaLabel) {
        this.ctaLabel = ctaLabel;
    }

    public String getCtaLink() {
        return ctaLink;
    }

    public void setCtaLink(String ctaLink) {
        this.ctaLink = ctaLink;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean isPinned) {
        this.isPinned = isPinned;
    }

    public boolean isPromotional() {
        return isPromotional;
    }

    public void setPromotional(boolean isPromotional) {
        this.isPromotional = isPromotional;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Post getParentPost() {
        return parentPost;
    }

    public void setParentPost(Post parentPost) {
        this.parentPost = parentPost;
    }

    public ProductServiceItem getTaggedProduct() {
        return taggedProduct;
    }

    public void setTaggedProduct(ProductServiceItem taggedProduct) {
        this.taggedProduct = taggedProduct;
    }

    public long getReachCount() {
        return reachCount;
    }

    public void setReachCount(long reachCount) {
        this.reachCount = reachCount;
    }

    public long getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(long sharesCount) {
        this.sharesCount = sharesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class PostBuilder {
        private Long id;
        private String content;
        private String hashtags;
        private User author;
        private LocalDateTime createdAt;
        private String mediaUrl;
        private int likesCount;
        private int commentsCount;
        private String ctaLabel;
        private String ctaLink;
        private LocalDateTime scheduledFor;
        private boolean isPinned;
        private boolean isPromotional;
        private boolean isPublished = true;
        private Post parentPost;
        private ProductServiceItem taggedProduct;
        private long reachCount;
        private long sharesCount;

        PostBuilder() {
        }

        public PostBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PostBuilder content(String content) {
            this.content = content;
            return this;
        }

        public PostBuilder hashtags(String hashtags) {
            this.hashtags = hashtags;
            return this;
        }

        public PostBuilder author(User author) {
            this.author = author;
            return this;
        }

        public PostBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PostBuilder mediaUrl(String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        public PostBuilder likesCount(int likesCount) {
            this.likesCount = likesCount;
            return this;
        }

        public PostBuilder commentsCount(int commentsCount) {
            this.commentsCount = commentsCount;
            return this;
        }

        public PostBuilder ctaLabel(String ctaLabel) {
            this.ctaLabel = ctaLabel;
            return this;
        }

        public PostBuilder ctaLink(String ctaLink) {
            this.ctaLink = ctaLink;
            return this;
        }

        public PostBuilder scheduledFor(LocalDateTime scheduledFor) {
            this.scheduledFor = scheduledFor;
            return this;
        }

        public PostBuilder isPinned(boolean isPinned) {
            this.isPinned = isPinned;
            return this;
        }

        public PostBuilder isPromotional(boolean isPromotional) {
            this.isPromotional = isPromotional;
            return this;
        }

        public PostBuilder isPublished(boolean isPublished) {
            this.isPublished = isPublished;
            return this;
        }

        public PostBuilder parentPost(Post parentPost) {
            this.parentPost = parentPost;
            return this;
        }

        public PostBuilder taggedProduct(ProductServiceItem taggedProduct) {
            this.taggedProduct = taggedProduct;
            return this;
        }

        public PostBuilder reachCount(long reachCount) {
            this.reachCount = reachCount;
            return this;
        }

        public PostBuilder sharesCount(long sharesCount) {
            this.sharesCount = sharesCount;
            return this;
        }

        public Post build() {
            return new Post(id, content, hashtags, author, createdAt, mediaUrl, likesCount, commentsCount, ctaLabel,
                    ctaLink, scheduledFor, isPinned, isPromotional, isPublished, parentPost, taggedProduct,
                    reachCount, sharesCount);
        }

    }
}
