package org.art.mt.dto;

import java.time.LocalDateTime;

public class PostDTO {

    private Long id;
    private String content;
    private UserDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PostDTO() {}
    public PostDTO(Long id, String content, UserDTO author, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    public UserDTO getAuthor() {return author;}
    public void setAuthor(UserDTO author) {this.author = author;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
    public LocalDateTime getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
