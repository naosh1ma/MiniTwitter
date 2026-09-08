package org.art.mt.controller;

import org.art.mt.dto.ProfileUpdateDTO;
import org.art.mt.dto.UserRegistrationDTO;
import org.art.mt.entity.User;
import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.UserDTO;
import org.art.mt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    if (userService.registerUser(dto.getUsername(), dto.getEmail(), dto.getPassword())) {
        return ResponseEntity.ok(ApiResponse.ok(null, "User registered successfully"));
    }
    return ResponseEntity.badRequest().body(ApiResponse.error("User registration failed"));
}

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(user), "User fetched")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile() {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(user), "Profile fetched")))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("User not found")));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(@Valid @RequestBody ProfileUpdateDTO dto) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updated = userService.updateProfile(username, dto.getBio(), dto.getAvatarUrl());
        return ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(updated), "Profile updated"));
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<UserDTO>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String username = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updated = userService.updateAvatar(username, file);
        return ResponseEntity.ok(ApiResponse.ok(userService.convertToDTOWithFollowInfo(updated), "Avatar updated"));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@PathVariable String username) {
        String currentUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.followUser(currentUsername, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Followed"));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable String username) {
        String currentUsername = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.unfollowUser(currentUsername, username);
        return ResponseEntity.ok(ApiResponse.ok(null, "Unfollowed"));
    }
}
