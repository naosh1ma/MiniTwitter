package org.art.mt.controller;

import org.art.mt.dto.UserRegistrationDTO;
import org.art.mt.entity.User;
import org.art.mt.dto.ApiResponse;
import org.art.mt.dto.UserDTO;
import org.art.mt.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(User -> convertToDTO(User))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
