package org.art.mt.dto;

import java.util.List;

public class PostFeedDTO {

    private List<PostDTO> posts;
    public PostFeedDTO() {}
    public PostFeedDTO(List<PostDTO> posts) {
        this.posts = posts;
    }
    public List<PostDTO> getPosts() {return posts;}
    public void setPosts(List<PostDTO> posts) {this.posts = posts;}
}
