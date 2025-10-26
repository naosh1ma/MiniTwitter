package org.art.mt.dto;

import org.art.mt.entity.User;

public class CreatePostDTO {

    private String content;
    private User author;

    public CreatePostDTO() {}
    public CreatePostDTO(String content, User author) {
        this.content = content;
        this.author = author;
    }
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    public User getAuthor() {return author;}
    public void setAuthor(User author) {this.author = author;}
}
