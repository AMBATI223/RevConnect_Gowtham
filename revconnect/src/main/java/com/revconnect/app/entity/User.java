package com.revconnect.app.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @Column(name = "USER_ID")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Profile Details
    private String bio;
    private String profilePictureUrl;
    private String location;
    private String website;

    @Column(nullable = false)
    private boolean isPrivateProfile;

    // Notification Preferences
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean prefConnectionRequests = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean prefLikes = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean prefComments = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean prefFollows = true;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean prefShares = true;

    @Enumerated(EnumType.STRING)
    private Role role; // USER, ADMIN

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BusinessProfile businessProfile;

    public User() {
    }

    public User(Long id, String username, String email, String password, String bio, String profilePictureUrl,
            String location, String website, boolean isPrivateProfile, Role role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.location = location;
        this.website = website;
        this.isPrivateProfile = isPrivateProfile;
        this.role = role;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public boolean isPrivateProfile() {
        return isPrivateProfile;
    }

    public void setPrivateProfile(boolean privateProfile) {
        isPrivateProfile = privateProfile;
    }

    public boolean isPrefConnectionRequests() {
        return prefConnectionRequests;
    }

    public void setPrefConnectionRequests(boolean prefConnectionRequests) {
        this.prefConnectionRequests = prefConnectionRequests;
    }

    public boolean isPrefLikes() {
        return prefLikes;
    }

    public void setPrefLikes(boolean prefLikes) {
        this.prefLikes = prefLikes;
    }

    public boolean isPrefComments() {
        return prefComments;
    }

    public void setPrefComments(boolean prefComments) {
        this.prefComments = prefComments;
    }

    public boolean isPrefFollows() {
        return prefFollows;
    }

    public void setPrefFollows(boolean prefFollows) {
        this.prefFollows = prefFollows;
    }

    public boolean isPrefShares() {
        return prefShares;
    }

    public void setPrefShares(boolean prefShares) {
        this.prefShares = prefShares;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BusinessProfile getBusinessProfile() {
        return businessProfile;
    }

    public void setBusinessProfile(BusinessProfile businessProfile) {
        this.businessProfile = businessProfile;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private String bio;
        private String profilePictureUrl;
        private String location;
        private String website;
        private boolean isPrivateProfile;
        private Role role;

        UserBuilder() {
        }

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            this.username = username;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public UserBuilder profilePictureUrl(String profilePictureUrl) {
            this.profilePictureUrl = profilePictureUrl;
            return this;
        }

        public UserBuilder location(String location) {
            this.location = location;
            return this;
        }

        public UserBuilder website(String website) {
            this.website = website;
            return this;
        }

        public UserBuilder isPrivateProfile(boolean isPrivateProfile) {
            this.isPrivateProfile = isPrivateProfile;
            return this;
        }

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public User build() {
            return new User(id, username, email, password, bio, profilePictureUrl, location, website, isPrivateProfile,
                    role);
        }
    }
}
