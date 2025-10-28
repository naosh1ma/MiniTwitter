package org.art.mt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostDTO {

    @NotBlank
    @Size(max = 280)
    private String content;

    public CreatePostDTO() {}

    public CreatePostDTO(String content) {
        this.content = content;
    }
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
}
