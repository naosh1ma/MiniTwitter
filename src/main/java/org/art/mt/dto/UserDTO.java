package org.art.mt.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;

    public UserDTO() {}
    
    public UserDTO(Long id, String username, String email, String bio, String avatarUrl, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getBio() {
        return bio;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
