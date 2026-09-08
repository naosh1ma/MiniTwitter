package org.art.mt.dto;

import jakarta.validation.constraints.Size;

public class ProfileUpdateDTO {

    @Size(max = 500)
    private String bio;

    private String avatarUrl;

    public ProfileUpdateDTO() {}

    public String getBio() {return bio;}
    public void setBio(String bio) {this.bio = bio;}

    public String getAvatarUrl() {return avatarUrl;}
    public void setAvatarUrl(String avatarUrl) {this.avatarUrl = avatarUrl;}
}
