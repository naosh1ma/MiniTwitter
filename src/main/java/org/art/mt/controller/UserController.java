package org.art.mt.controller;

import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.ProfileUpdateDTO;
import org.art.mt.dto.UserDTO;
import org.art.mt.dto.UserRegistrationDTO;
import org.art.mt.entity.User;
import org.art.mt.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody UserRegistrationDTO dto) {
        userService.registerUser(dto.getUsername(), dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(ApiResponse.ok(null, "User registered successfully"));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        return profileResponse(username, "User fetched");
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(@AuthenticationPrincipal String username) {
        return profileResponse(username, "Profile fetched");
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto,
                                                              @AuthenticationPrincipal String username) {
        User updated = userService.updateProfile(username, dto.getBio(), dto.getAvatarUrl());
        return ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(updated), "Profile updated"));
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                             @AuthenticationPrincipal String username) {
        User updated = userService.updateAvatar(username, file);
        return ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(updated), "Avatar updated"));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@PathVariable String username,
                                                    @AuthenticationPrincipal String currentUsername) {
        userService.followUser(currentUsername, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Followed"));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable String username,
                                                      @AuthenticationPrincipal String currentUsername) {
        userService.unfollowUser(currentUsername, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Unfollowed"));
    }

    private ResponseEntity<ApiResponse<UserDTO>> profileResponse(String username, String message) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(user), message)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("User not found")));
    }
}
