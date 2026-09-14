package org.art.mt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentDTO {

    @NotBlank
    @Size(max = 280)
    private String content;

    public CreateCommentDTO() {}

    public CreateCommentDTO(String content) {
        this.content = content;
    }
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
}
